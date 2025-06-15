package com.polygonbikes.ebike.v3.feature_route.domain.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.domain.model.RouteFilterData

data class FriendsRouteState (
    val listFriendsRoute: SnapshotStateList<RouteData> = mutableStateListOf(),
    val loadingMoreData: Boolean = false,
    val isRefreshing: Boolean = false,
    val isFiltering: Boolean = false,
    val lastEvaluatedKey: String? = null,
    val isLoading: Boolean = false,
    var showRouteFilter: Boolean = false,
    val routeFilterData: RouteFilterData = RouteFilterData(),
    val listCityFilter: SnapshotStateList<LocationData> = mutableStateListOf()

)