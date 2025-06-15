package com.polygonbikes.ebike.v3.feature_history.domain.state

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware
import kotlinx.coroutines.flow.MutableStateFlow

data class DetailHistoryState (
    val isLoading: Boolean? = false,
    val tripName: String = "",
    val trip: TripBodyMiddleware? = null,
    val routeWaypoint: List<LatLng> = mutableListOf(),
    val bounds: MutableStateFlow<LatLngBounds?> = MutableStateFlow(null),
    val fitFile: String? = null,
    val alreadyShareToStrava: Boolean = false,
    val connectedToStrava: Boolean = false,
    val queueUploadStrava: Boolean = false,
    val comment: String = "",
    val isSendCommentEnabled: Boolean = true
)