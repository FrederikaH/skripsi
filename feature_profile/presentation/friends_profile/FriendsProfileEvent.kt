package com.polygonbikes.ebike.v3.feature_profile.presentation.friends_profile

import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware

sealed class FriendsProfileEvent {
    data class GetTripSummary(val period: String, val userId: Int) : FriendsProfileEvent()
    object Follow : FriendsProfileEvent()
    object Unfollow : FriendsProfileEvent()
    data class GetUserActivity(val userId: Int) : FriendsProfileEvent()
    data class GetUserProfile(val userId: Int) : FriendsProfileEvent()
    data class OpenDetailActivity(val activity: TripBodyMiddleware) : FriendsProfileEvent()
}