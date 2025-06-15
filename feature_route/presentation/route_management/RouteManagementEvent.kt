package com.polygonbikes.ebike.v3.feature_route.presentation.route_management

import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.domain.model.RouteFilterData

sealed class RouteManagementEvent {
    data class OpenDetailRoute(val route: RouteData): RouteManagementEvent()
    object OpenCreateRoute: RouteManagementEvent()
    object OpenRouteStatistic: RouteManagementEvent()
    object ShowRouteFilter : RouteManagementEvent()
    data class UpdateRouteFilter(val routeFilterData: RouteFilterData) : RouteManagementEvent()
    data class DeleteRoute(val routeId: Int): RouteManagementEvent()
    data class RestoreRoute(val routeId: Int) : RouteManagementEvent()
    object InitDataChanges : RouteManagementEvent()
}