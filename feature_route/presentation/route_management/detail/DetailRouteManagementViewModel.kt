package com.polygonbikes.ebike.v3.feature_route.presentation.route_management.detail

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.navOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.polygonbikes.ebike.core.route.DetailRouteManagementRoute
import com.polygonbikes.ebike.core.route.EditRouteRoute
import com.polygonbikes.ebike.core.route.EventRoute
import com.polygonbikes.ebike.core.route.RouteManagementRoute
import com.polygonbikes.ebike.core.util.GooglePolylineUtils
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.Navigate
import com.polygonbikes.ebike.core.util.UiEvent.ShowSnackBar
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.DetailRouteResponseMiddleware
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.data.remote.RouteServiceMiddleware
import com.polygonbikes.ebike.v3.feature_route.domain.state.DetailRouteManagementState
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
class DetailRouteManagementViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val routeServiceMiddleware: RouteServiceMiddleware
) : ViewModel() {
    private var _state: MutableState<DetailRouteManagementState> = mutableStateOf(DetailRouteManagementState())
    val state: State<DetailRouteManagementState> get() = _state

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    lateinit var call: Call<RouteData>

    fun onEvent(event: DetailRouteManagementEvent) {
        when (event) {
            is DetailRouteManagementEvent.GetData -> {
                handlePolylineData(event.route)
                event.route.gpx?.url?.let { gpxUrl ->
                    fetchAndParseGpx(gpxUrl)
                }

                Log.d("DetailRouteManagement", "route data on detail: ${state.value.route?.deletedAt}")
                getRouteDetail(routeId = state.value.route?.id ?: event.route.id)
            }

            DetailRouteManagementEvent.DeleteRoute -> {
                _state.value = state.value.copy(isLoading = true)

                state.value.route?.id?.let { routeId ->
                    routeServiceMiddleware.deleteRoute(routeId = routeId)
                        .enqueue(object : Callback<RouteData> {
                            override fun onResponse(call: Call<RouteData>, response: Response<RouteData>) {
                                viewModelScope.launch {
                                    if (response.isSuccessful) {
                                        sendUiEvent(Navigate(route = RouteManagementRoute))
                                        Toast.makeText(context, "Success to delete route", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to delete route", Toast.LENGTH_SHORT).show()
                                    }
                                    _state.value = state.value.copy(isLoading = false)
                                }
                            }

                            override fun onFailure(call: Call<RouteData>, t: Throwable) {
                                _state.value = state.value.copy(isLoading = false)
                                Toast.makeText(context, "Failed to delete route", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }

            DetailRouteManagementEvent.RestoreRoute -> {
                _state.value = state.value.copy(isLoading = true)

                state.value.route?.id?.let { routeId ->
                    routeServiceMiddleware.restoreRoute(routeId = routeId)
                        .enqueue(object : Callback<RouteData> {
                            override fun onResponse(call: Call<RouteData>, response: Response<RouteData>) {
                                viewModelScope.launch {
                                    if (response.isSuccessful) {
                                        sendUiEvent(Navigate(route = RouteManagementRoute))
                                        Toast.makeText(context, "Success to restore route", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to restore route", Toast.LENGTH_SHORT).show()
                                    }
                                    _state.value = state.value.copy(isLoading = false)
                                }
                            }

                            override fun onFailure(call: Call<RouteData>, t: Throwable) {
                                _state.value = state.value.copy(isLoading = false)
                                Toast.makeText(context, "Failed to restore route", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }

            DetailRouteManagementEvent.EditRoute -> {
                val detail = state.value.route
                _state.value = state.value.copy(isLoading = true)
                Log.d("DetailRouteManagement", "EditRoute clicked — detail = $detail")

                state.value.route?.let {
                    _state.value = state.value.copy(isLoading = false)

                    sendUiEvent(
                        Navigate(
                            route = EditRouteRoute(it),
                        )
                    )
                }
                _state.value = state.value.copy(isLoading = false)
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

                        _state.value = state.value.copy(
                            route = routeData,
                            isLoading = false,
                            isDeleted = !routeData?.deletedAt.isNullOrBlank()
                        )
                        Log.d("RouteDetail", "isDeleted: ${state.value.isDeleted}, deletedAt: ${state.value.route?.deletedAt}")

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