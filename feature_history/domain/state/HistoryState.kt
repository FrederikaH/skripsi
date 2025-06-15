package com.polygonbikes.ebike.v3.feature_history.domain.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware

data class HistoryState (
    val listHistory: SnapshotStateList<TripBodyMiddleware> = mutableStateListOf(),
//    val isLoading: Boolean = false,
    val isHistoryLoading: Boolean = true,
    val userId: Long? = null,
    val period: String? = "all",
    val isHistoryEmpty: Boolean? = true
)