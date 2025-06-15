package com.polygonbikes.ebike.v3.feature_profile.domain.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileData
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.TripSummaryData
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.TripSummaryResponse
import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware

data class FriendsProfileState (
    val profile: ProfileData? = null,
    val tripSummary: TripSummaryData? = null,
    val period: String? = "month",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val listActivity: SnapshotStateList<TripBodyMiddleware> = mutableStateListOf(),
    val userId: Int = 0,
    val isOthersProfile: Boolean = true,
    val isActivityLoading: Boolean = true
)