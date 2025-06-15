package com.polygonbikes.ebike.v3.feature_route.presentation.friends

import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.domain.model.RouteFilterData

sealed class FriendsRouteEvent {
    data class OpenRouteDetail(val route: RouteData): FriendsRouteEvent()
    object ShowRouteFilter : FriendsRouteEvent()
    data class UpdateRouteFilter(val routeFilterData: RouteFilterData) : FriendsRouteEvent()

}