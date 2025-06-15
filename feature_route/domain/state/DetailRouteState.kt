package com.polygonbikes.ebike.v3.feature_route.domain.state

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.polygonbikes.ebike.core.enums.ProcessGPXStatus
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import kotlinx.coroutines.flow.MutableStateFlow

data class DetailRouteState (
    val isLoading: Boolean? = false,
    val route: RouteData? = null,
    val routeWaypoint: List<LatLng> = mutableListOf(),
    val bounds: MutableStateFlow<LatLngBounds?> = MutableStateFlow(null),
    val gpxStatus: ProcessGPXStatus? = null,
    val stateLoadPolyline: MutableStateFlow<LoadPolylineDetailRoad> = MutableStateFlow(
        LoadPolylineDetailRoad()
    ),
    val saveRouteName: String = "",
    val saveRouteId: Int = 0,
    var showDialog: Boolean = false,
    val comment: String = "",
    val isSendCommentEnabled: Boolean = true
)

data class LoadPolylineDetailRoad(
    val mapLoaded: Boolean = false,
    val routeWaypoint: List<LatLng> = listOf()
)