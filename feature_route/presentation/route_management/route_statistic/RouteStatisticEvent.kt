package com.polygonbikes.ebike.v3.feature_route.presentation.route_management.route_statistic

import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData

sealed class RouteStatisticEvent {
    data class OpenDetailRoute(val route: RouteData): RouteStatisticEvent()
    data class SetValuePeriod(val value: String) : RouteStatisticEvent()
}