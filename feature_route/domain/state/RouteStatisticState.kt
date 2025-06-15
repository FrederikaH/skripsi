package com.polygonbikes.ebike.v3.feature_route.domain.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RoadTypeCountResponseMiddleware
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RoadTypeData
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData

data class RouteStatisticState (
    val listMostSavedRoute: SnapshotStateList<RouteData> = mutableStateListOf(),
    val roadTypesCount: RoadTypeData? = null,
    val isMostSavedRouteLoading: Boolean = true,
    val isRoadTypeLoading: Boolean = true,
    val period: String ? = "month"
)