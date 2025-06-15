package com.polygonbikes.ebike.v3.feature_route.domain.state

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.polygonbikes.ebike.core.enums.ProcessGPXStatus
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import kotlinx.coroutines.flow.MutableStateFlow

data class DetailRouteManagementState (
    val isLoading: Boolean? = false,
    val route: RouteData? = null,
    val routeWaypoint: List<LatLng> = mutableListOf(),
    val bounds: MutableStateFlow<LatLngBounds?> = MutableStateFlow(null),
    val gpxStatus: ProcessGPXStatus? = null,
    val stateLoadPolyline: MutableStateFlow<LoadPolylineDetailRoad> = MutableStateFlow(LoadPolylineDetailRoad()),
    val isDeleted: Boolean? = false
)