package com.polygonbikes.ebike.v3.feature_profile.domain.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.polygonbikes.ebike.v3.feature_event.data.entities.EventData
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileData
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.TripSummaryData
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.TripSummaryResponse

data class ProfileState (
    val profile: ProfileData? = null,
    val tripSummary: TripSummaryData? = null,
    val period: String? = "month",
    val isLoading: Boolean = false,
    val isProfileLoading: Boolean = true,
    val isTripSummaryLoading: Boolean = true,
    val isUpcomingEventLoading: Boolean = true,
    val errorMessage: String? = null,
    val listUpcomingEvent: SnapshotStateList<EventData> = mutableStateListOf()
)