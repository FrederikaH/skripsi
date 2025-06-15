package com.polygonbikes.ebike.v3.feature_route.presentation.detail

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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.polygonbikes.ebike.core.route.CreateEventRoute
import com.polygonbikes.ebike.core.util.GooglePolylineUtils
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.*
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.DetailRouteResponseMiddleware
import com.polygonbikes.ebike.core.model.MessageData
import com.polygonbikes.ebike.v3.feature_route.data.entities.body.SaveRouteBody
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.data.remote.RouteServiceMiddleware
import com.polygonbikes.ebike.v3.feature_route.domain.state.DetailRouteState
import com.polygonbikes.ebike.core.entities.response.CommentResponse
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

@HiltViewModel
class DetailRouteViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    savedStateHandle: SavedStateHandle,
    val routeServiceMiddleware: RouteServiceMiddleware
) : ViewModel() {
    private var _state: MutableState<DetailRouteState> = mutableStateOf(DetailRouteState())
    val state: State<DetailRouteState> get() = _state

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    lateinit var call: Call<RouteData>

    fun onEvent(event: DetailRouteEvent) {
        when (event) {
            is DetailRouteEvent.GetData -> {
                handlePolylineData(event.route)
                event.route.gpx?.url?.let { gpxUrl ->
                    fetchAndParseGpx(gpxUrl)
                }

                state.value.route?.let { getRouteDetail(routeId = it.id)}
            }

            is DetailRouteEvent.DownloadGPX -> {
                state.value.route?.gpx?.url?.let { gpxUrl ->
                    downloadGPXFile(context, gpxUrl)
                }
            }

            is DetailRouteEvent.SaveRoute -> {
                _state.value = state.value.copy(
                    saveRouteName = event.name,
                    isLoading = true
                )

                val call: Call<RouteData> = routeServiceMiddleware.saveRoute(
                    routeId = state.value.route?.id ?: 0,
                    body = SaveRouteBody(event.name)
                )

                call.enqueue(object : Callback<RouteData> {
                    override fun onResponse(
                        call: Call<RouteData>,
                        response: Response<RouteData>
                    ) {
                        _state.value = state.value.copy(isLoading = false)

                        if (response.isSuccessful) {
                            Log.d("SaveRoute", "Route saved successfully: ${event.name}")
                        } else {
                            Log.e("SaveRoute", "Error: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<RouteData>, t: Throwable) {
                        Log.e("SaveRoute", "onFailure: ${t.message}")
                        _state.value = state.value.copy(isLoading = false)
                    }
                })
            }

            is DetailRouteEvent.SaveRouteDialog -> {
                val newState = !state.value.showDialog
                Log.d("SaveRouteDialog", "Toggling showDialog: $newState")
                _state.value = state.value.copy(showDialog = newState)
            } // nanti stlh disave, diarahkan ke detail baru berisi route yg disave td

            is DetailRouteEvent.OpenCreateEventScreen -> {
                sendUiEvent(
                    Navigate(route = CreateEventRoute)
                )
            }

            is DetailRouteEvent.OnInputComment -> {
                _state.value = state.value.copy(comment = event.comment)
            }

            DetailRouteEvent.SendComment -> {
                val call: Call<CommentResponse> = routeServiceMiddleware.addComment(
                    routeId = state.value.route?.id ?: 0,
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
                                    routeServiceMiddleware.getDetailRoute(
                                        routeId = state.value.route?.id ?: 0,
                                        embed = "comments"
                                    ).enqueue(object : Callback<DetailRouteResponseMiddleware> {
                                        override fun onResponse(
                                            call: Call<DetailRouteResponseMiddleware>,
                                            response: Response<DetailRouteResponseMiddleware>
                                        ) {
                                            if (response.isSuccessful) {
                                                val fetchedDetailRoute = response.body()?.data

                                                _state.value = state.value.copy(
                                                    route = fetchedDetailRoute,
                                                    comment = ""
                                                )
                                                fetchedDetailRoute?.id?.let { routeId ->
                                                    getRouteDetail(routeId)
                                                }

                                            } else {
                                                sendUiEvent(ShowSnackBar(message = "Comment failed to send"))
                                            }
                                            _state.value = state.value.copy(isSendCommentEnabled = true)
                                        }

                                        override fun onFailure(
                                            call: Call<DetailRouteResponseMiddleware>,
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
                        _state.value = state.value.copy(isSendCommentEnabled = true)
                        sendUiEvent(ShowSnackBar(message = "Comment failed to send"))
                    }
                })
            }

            is DetailRouteEvent.OnSetCommentSending -> {
                if (event.isEnable == true) {
                    _state.value = state.value.copy(isSendCommentEnabled = true)
                } else {
                    _state.value = state.value.copy(isSendCommentEnabled = false)
                }
            }
        }
    }

    private fun handlePolylineData(route: RouteData) {
        val polyline = route.polyline.orEmpty()

        if (polyline.length < 16) {
            Log.e("PolylineError", "Invalid polyline: $polyline")
            return
        }

        val decodedPolyline = GooglePolylineUtils.decode(polyline)
        _state.value = state.value.copy(
            routeWaypoint = decodedPolyline,
            route = route
        )
        _state.value.bounds.update { decodedPolyline.getBounds() }
    }

    private fun fetchAndParseGpx(gpxUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val gpxFile = downloadFileFromUrl(gpxUrl)
                val gpxOutput = GPX.read(gpxFile.toPath())

                if (gpxOutput.tracks.isEmpty()) {
                    sendUiEvent(ShowSnackBar(message = "GPX file has no tracks"))
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

    private fun getRouteDetail(routeId: Int) {
        _state.value = state.value.copy(isLoading = true)

        routeServiceMiddleware.getDetailRoute(routeId = routeId, embed = "comments")
            .enqueue(object : Callback<DetailRouteResponseMiddleware> {
                override fun onResponse(
                    call: Call<DetailRouteResponseMiddleware>,
                    response: Response<DetailRouteResponseMiddleware>
                ) {
                    if (response.isSuccessful) {
                        val routeData = response.body()?.data

                        Log.d(
                            "RouteDetail",
                            "Route data fetched: ${routeData?.comments?.size} comments"
                        )

                        _state.value = state.value.copy(
                            route = routeData,
                            isLoading = false
                        )
                    } else {
                        Log.e("RouteDetail", "Failed with code: ${response.code()}")
                        _state.value = state.value.copy(isLoading = false)
                    }
                }

                override fun onFailure(call: Call<DetailRouteResponseMiddleware>, t: Throwable) {
                    Log.e("RouteDetail", "Error fetching route detail: ${t.message}")
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