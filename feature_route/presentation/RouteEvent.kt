package com.polygonbikes.ebike.v3.feature_route.presentation

import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.domain.model.RouteFilterData

sealed class RouteEvent {
    data class OpenRouteDetail(val route: RouteData): RouteEvent()
    object OpenFriendsRoute: RouteEvent()
    object ShowRouteFilter : RouteEvent()
    data class UpdateRouteFilter(val routeFilterData: RouteFilterData) : RouteEvent()
}