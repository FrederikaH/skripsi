package com.polygonbikes.ebike.v3.feature_home.presentation.detail_activity

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.polygonbikes.ebike.core.entities.response.CommentResponse
import com.polygonbikes.ebike.core.model.MessageData
import com.polygonbikes.ebike.core.util.GooglePolylineUtils
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.ShowSnackBar
import com.polygonbikes.ebike.v3.feature_home.data.entities.DetailTripBodyMiddleware
import com.polygonbikes.ebike.v3.feature_home.domain.state.DetailActivityState
import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware
import com.polygonbikes.ebike.v3.feature_trip.data.remote.TripServiceMiddleware
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.jenetics.jpx.GPX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.URL
import javax.inject.Inject
import kotlin.collections.forEach

@HiltViewModel
class DetailActivityViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    savedStateHandle: SavedStateHandle,
    val tripServiceMiddleware: TripServiceMiddleware
) : ViewModel() {

    private val TAG = "DetailActivityVM"
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var _state: MutableState<DetailActivityState> = mutableStateOf(DetailActivityState())
    val state: State<DetailActivityState> get() = _state

    fun onEvent(event: DetailActivityEvent) {
        when (event) {
            is DetailActivityEvent.GetData -> {
                _state.value = state.value.copy(trip = event.activity)

                handlePolylineData(event.activity)
                event.activity.route?.gpx?.url?.let { gpxUrl ->
                    fetchAndParseGpx(gpxUrl)
                }
                state.value.trip?.id?.let { getTripDetail(it) }

            }

            is DetailActivityEvent.OnInputComment -> {
                _state.value = state.value.copy(comment = event.comment)
            }

            DetailActivityEvent.SendComment -> {
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
                                    state.value.trip?.id?.let { tripId ->
                                        tripServiceMiddleware.getDetailTrip(
                                            tripId = tripId,
                                            embed = "comments"
                                        )
                                    }?.enqueue(object : Callback<DetailTripBodyMiddleware> {
                                        override fun onResponse(
                                            call: Call<DetailTripBodyMiddleware>,
                                            response: Response<DetailTripBodyMiddleware>
                                        ) {
                                            if (response.isSuccessful) {
                                                val fetchedDetailActivity = response.body()?.data

                                                if (fetchedDetailActivity != null) {
                                                    _state.value = state.value.copy(
                                                        trip = fetchedDetailActivity,
                                                        comment = ""
                                                    )
                                                    fetchedDetailActivity.id.let { tripId ->
                                                        getTripDetail(tripId = tripId)
                                                    }
                                                } else {
                                                    sendUiEvent(ShowSnackBar(message = "Failed to load trip detail."))
                                                }

                                            } else {
                                                sendUiEvent(ShowSnackBar(message = "Comment failed to send"))
                                            }
                                            _state.value = state.value.copy(isSendCommentEnabled = true)
                                        }

                                        override fun onFailure(
                                            call: Call<DetailTripBodyMiddleware>,
                                            t: Throwable
                                        ) {
                                            _state.value = state.value.copy(isSendCommentEnabled = true)
                                            sendUiEvent(ShowSnackBar(message = "Comment failed to send"))
                                        }
                                    })
                                }
                            }
                        } else {
                            _state.value = state.value.copy(isSendCommentEnabled = true)
                            sendUiEvent(ShowSnackBar(message = "Comment failed to send"))
                        }
                    }

                    override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                        sendUiEvent(ShowSnackBar(message = "Comment failed to send"))
                    }
                })
            }

            is DetailActivityEvent.OnSetCommentSending -> {
                if (event.isEnable == true) {
                    _state.value = state.value.copy(isSendCommentEnabled = true)
                } else {
                    _state.value = state.value.copy(isSendCommentEnabled = false)
                }
            }
        }
    }

    private fun handlePolylineData(activity: TripBodyMiddleware) {
        val decodedPolyline = GooglePolylineUtils.decode(activity.route?.polyline ?: "")
            _state.value = state.value.copy(
                routeWaypoint = decodedPolyline,
                trip = activity
            )
            _state.value.bounds.update { decodedPolyline.getBounds() }
    }

    private fun fetchAndParseGpx(gpxUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val gpxFile = downloadFileFromUrl(gpxUrl)
                val gpxOutput = GPX.read(gpxFile.toPath())

                if (gpxOutput.tracks.isEmpty()) {
                    return@launch sendUiEvent(ShowSnackBar(message = "GPX file has no tracks"))
                }

                val routeWaypoints = gpxOutput.tracks.flatMap { it.segments }
                    .flatMap { it.points }
                    .map { LatLng(it.latitude.toDegrees(), it.longitude.toDegrees()) }

                _state.value = state.value.copy(routeWaypoint = routeWaypoints)
                _state.value.bounds.update { routeWaypoints.getBounds() }

            } catch (e: Exception) {
                sendUiEvent(ShowSnackBar(message = "Failed to load GPX"))
            }
        }
    }

    private fun downloadFileFromUrl(url: String): File {
        return File.createTempFile("temp_gpx_", ".gpx").apply {
            URL(url).openStream().use { input -> outputStream().use { input.copyTo(it) } }
        }
    }

    // Extension function to calculate bounds
    private fun List<LatLng>.getBounds(): LatLngBounds =
        LatLngBounds.Builder().apply { forEach { include(it) } }.build()

    private fun getTripDetail(tripId: Int) {
        _state.value = state.value.copy(isLoading = true)

        tripServiceMiddleware.getDetailTrip(tripId = tripId, embed = "comments")
            .enqueue(object : Callback<DetailTripBodyMiddleware> {
                override fun onResponse(
                    call: Call<DetailTripBodyMiddleware>,
                    response: Response<DetailTripBodyMiddleware>
                ) {
                    if (response.isSuccessful) {
                        val tripData = response.body()?.data

                        Log.d(
                            "TripDetail",
                            "Trip data fetched: ${tripData?.comments?.size} comments"
                        )

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