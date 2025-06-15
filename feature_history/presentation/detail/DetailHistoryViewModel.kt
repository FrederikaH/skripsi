package com.polygonbikes.ebike.v3.feature_history.presentation.detail

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.polygonbikes.ebike.core.dao.PendingUploadStravaDao
import com.polygonbikes.ebike.core.database.LogDatabase
import com.polygonbikes.ebike.core.entities.PendingUploadStrava
import com.polygonbikes.ebike.core.route.StravaRoute
import com.polygonbikes.ebike.core.util.Constant
import com.polygonbikes.ebike.core.util.GooglePolylineUtils
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.stravamanager.StravaManager
import com.polygonbikes.ebike.feature_strava.workmanager.UploadStravaWorker
import com.polygonbikes.ebike.v3.feature_history.domain.state.DetailHistoryState
import com.polygonbikes.ebike.v3.feature_home.data.entities.DetailTripBodyMiddleware
import com.polygonbikes.ebike.core.model.MessageData
import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware
import com.polygonbikes.ebike.core.entities.response.CommentResponse
import com.polygonbikes.ebike.v3.feature_trip.data.remote.TripServiceMiddleware
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.jenetics.jpx.GPX
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DetailHistoryViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    savedStateHandle: SavedStateHandle,
    private val workManager: WorkManager,
    private val stravaManager: StravaManager,
    private val tripServiceMiddleware: TripServiceMiddleware
) : ViewModel() {
    private val TAG = "DetailHistoryVM"
    private var _state: MutableState<DetailHistoryState> = mutableStateOf(DetailHistoryState())
    val state: State<DetailHistoryState> get() = _state

    private val roomDB: LogDatabase = LogDatabase.getDatabase(context)
    private val pendingUploadStravaDao: PendingUploadStravaDao = roomDB.pendingUploadStravaDao()

    private var jobListener: CompletableJob? = null
    private var scopeListener: CoroutineScope? = null

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        val tripName: String = savedStateHandle.get<String>("name") ?: ""
        _state.value = _state.value.copy(
            tripName = tripName
        )
    }

    fun onEvent(event: DetailHistoryEvent) {
        when (event) {
            is DetailHistoryEvent.InitScreen -> {
                _state.value = state.value.copy(
                    connectedToStrava = stravaManager.isLoggedIn()
                )
            }

            is DetailHistoryEvent.GetData -> {
                val decodedPolyline =
                    GooglePolylineUtils.decode(event.trip.route?.polyline ?: "")

                _state.value = state.value.copy(
                    routeWaypoint = decodedPolyline,
                    trip = event.trip
                )

                val builderLatLngBounds = LatLngBounds.Builder()
                for (item in decodedPolyline) {
                    builderLatLngBounds.include(item)
                }
                val bounds = builderLatLngBounds.build()
                _state.value.bounds.update { bounds }

                handlePolylineData(event.trip)
                event.trip.route?.gpx?.url.let { gpxUrl ->
                    fetchAndParseGpx(gpxUrl.toString())
                }

                val activityId: String = event.trip.id.toString()
                Log.d(TAG, "ActivityID: $activityId")

                if (scopeListener != null) {
                    scopeListener?.cancel()
                    jobListener?.cancel()
                }

                jobListener = SupervisorJob()
                scopeListener = CoroutineScope(Dispatchers.IO + jobListener!!)
                scopeListener?.launch {
                    pendingUploadStravaDao
                        .flowFetchById(activityId = activityId)
                        .collect {
                            viewModelScope.launch {
                                if (state.value.queueUploadStrava && it == null) {
                                    _state.value = state.value.copy(
                                        alreadyShareToStrava = true,
                                        queueUploadStrava = false
                                    )
                                } else if (!state.value.queueUploadStrava && it != null) {
                                    _state.value = state.value.copy(queueUploadStrava = true)
                                }
                            }
                        }
                }

                _state.value = state.value.copy(
                    alreadyShareToStrava = event.trip.strava?.activity_id != null && event.trip.strava?.activity_id != 0L
                )

                state.value.trip?.let { getTripDetail(tripId = it.id) }
            }

            is DetailHistoryEvent.OpenConnectStrava -> {
                sendUiEvent(
                    UiEvent.Navigate(
                        route = StravaRoute(
                            code = null,
                            scope = null,
                            error = null
                        )
                    )
                )
            }

            is DetailHistoryEvent.ShareToStrava -> {
                if (state.value.trip?.id == null) return

                val constraints = Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val request = OneTimeWorkRequestBuilder<UploadStravaWorker>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 15L, TimeUnit.MINUTES)
                    .build()

                viewModelScope.launch {
                    _state.value = state.value.copy(
                        queueUploadStrava = true
                    )

                    val newPending = PendingUploadStrava(
                        activityId = state.value.trip?.id!!.toString()
                    )
                    pendingUploadStravaDao.insert(newPending)
                    workManager.enqueueUniqueWork(
                        Constant.TAG_UPLOADSTRAVA_WORKMANAGER,
                        ExistingWorkPolicy.REPLACE,
                        request
                    )
                }
            }

            is DetailHistoryEvent.OnInputComment -> {
                _state.value = state.value.copy(comment = event.comment)
            }

            DetailHistoryEvent.SendComment -> {
                val call: Call<CommentResponse> = tripServiceMiddleware.addComment(
                    tripId = state.value.trip?.id ?: 0,
                    message = MessageData(state.value.comment)
                )

                if (state.value.comment.isEmpty()) {
                    return
                }

                call.enqueue(object : Callback<CommentResponse> {
                    override fun onResponse(
                        call: Call<CommentResponse>,
                        response: Response<CommentResponse>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.data?.let {
                                viewModelScope.launch {
                                    _state.value = state.value.copy(comment = "")
                                    tripServiceMiddleware.getDetailTrip(
                                        tripId = state.value.trip?.id ?: 0,
                                        embed = "user,comments"
                                    ).enqueue(object : Callback<DetailTripBodyMiddleware> {
                                        override fun onResponse(
                                            call: Call<DetailTripBodyMiddleware>,
                                            response: Response<DetailTripBodyMiddleware>
                                        ) {
                                            if (response.isSuccessful) {
                                                val fetchedDetailTrip = response.body()?.data

                                                _state.value = state.value.copy(
                                                    trip = fetchedDetailTrip,
                                                    comment = ""
                                                )
                                                fetchedDetailTrip?.id?.let { tripId ->
                                                    getTripDetail(tripId)
                                                }

                                            } else {
                                                sendUiEvent(UiEvent.ShowSnackBar(message = "Comment failed to send"))
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<DetailTripBodyMiddleware>,
                                            t: Throwable
                                        ) {
                                            sendUiEvent(UiEvent.ShowSnackBar(message = "Comment failed to send"))
                                        }
                                    })
                                }
                            }
                        } else {
                            sendUiEvent(UiEvent.ShowSnackBar(message = "Comment failed to send"))
                        }
                        _state.value = state.value.copy(isSendCommentEnabled = true)
                    }

                    override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                        _state.value = state.value.copy(isSendCommentEnabled = true)
                        sendUiEvent(UiEvent.ShowSnackBar(message = "Comment failed to send"))
                    }
                })
            }

            is DetailHistoryEvent.OnSetCommentSending -> {
                if (event.isEnable == true) {
                    _state.value = state.value.copy(isSendCommentEnabled = true)
                } else {
                    _state.value = state.value.copy(isSendCommentEnabled = false)
                }
            }
        }
    }

    private fun handlePolylineData(trip: TripBodyMiddleware) {
        val decodedPolyline = GooglePolylineUtils.decode(trip.route?.polyline ?: "")
        _state.value = state.value.copy(
            routeWaypoint = decodedPolyline,
        )
        _state.value.bounds.update { decodedPolyline.getBounds() }
    }

    private fun fetchAndParseGpx(gpxUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val gpxFile = downloadFileFromUrl(gpxUrl)
                val gpxOutput = GPX.read(gpxFile.toPath())

                if (gpxOutput.tracks.isEmpty()) {
                    sendUiEvent(UiEvent.ShowSnackBar(message = "GPX file has no tracks"))
                    return@launch
                }

                val routeWaypoints = gpxOutput.tracks.flatMap { it.segments }
                    .flatMap { it.points }
                    .map { LatLng(it.latitude.toDegrees(), it.longitude.toDegrees()) }

                _state.value = state.value.copy(routeWaypoint = routeWaypoints)
                _state.value.bounds.update { routeWaypoints.getBounds() }

            } catch (e: Exception) {
                sendUiEvent(UiEvent.ShowSnackBar(message = "Failed to load GPX"))
            }
        }
    }

    private fun downloadFileFromUrl(url: String): File {
        return File.createTempFile("temp_gpx_", ".gpx").apply {
            URL(url).openStream().use { input -> outputStream().use { input.copyTo(it) } }
        }
    }

    private fun List<LatLng>.getBounds(): LatLngBounds =
        LatLngBounds.Builder().apply { forEach { include(it) } }.build()

    private fun downloadGPXFile(context: Context, url: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("GPX file")
                .setDescription("Your GPX file is being downloaded.")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "track.gpx")

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            sendUiEvent(UiEvent.ShowSnackBar(message = "Downloading GPX..."))
        } catch (e: Exception) {
            sendUiEvent(UiEvent.ShowSnackBar(message = "Failed to download GPX"))
        }
    }

    private fun getTripDetail(tripId: Int) {
        _state.value = state.value.copy(isLoading = true)

        tripServiceMiddleware.getDetailTrip(tripId = tripId, embed = "user,comments")
            .enqueue(object : Callback<DetailTripBodyMiddleware> {
                override fun onResponse(
                    call: Call<DetailTripBodyMiddleware>,
                    response: Response<DetailTripBodyMiddleware>
                ) {
                    if (response.isSuccessful) {
                        val tripData = response.body()?.data
                        
                        _state.value = state.value.copy(
                            trip = tripData,
                            isLoading = false
                        )
                    } else {
                        Log.e("TripDetail", "Failed with code: ${response.code()}")
                        _state.value = state.value.copy(isLoading = false)
                    }
                }

                override fun onFailure(call: Call<DetailTripBodyMiddleware>, t: Throwable) {
                    Log.e("TripDetail", "Error fetching trip detail: ${t.message}")
                    _state.value = state.value.copy(isLoading = false)
                }
            })
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}
