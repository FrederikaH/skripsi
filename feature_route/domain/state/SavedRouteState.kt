package com.polygonbikes.ebike.v3.feature_route.domain.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData

data class SavedRouteState (
    val listRoute: SnapshotStateList<RouteData> = mutableStateListOf(),
    val loadingMoreData: Boolean = false,
    val isRefreshing: Boolean = false,
    val isFiltering: Boolean = false,
    val lastEvaluatedKey: String? = null,
    val isRouteLoading: Boolean = true
)