package com.polygonbikes.ebike.v3.feature_route.presentation.route_management.detail

import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData

sealed class DetailRouteManagementEvent {
    data class GetData(val route: RouteData) : DetailRouteManagementEvent()
    object EditRoute : DetailRouteManagementEvent()
    object DeleteRoute : DetailRouteManagementEvent()
    object RestoreRoute : DetailRouteManagementEvent()
}