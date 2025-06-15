package com.polygonbikes.ebike.v3.feature_home.presentation

import com.polygonbikes.ebike.v3.feature_event.data.entities.EventData
import com.polygonbikes.ebike.v3.feature_event.presentation.EventEvent
import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware

sealed class HomeEvent {
    data class OpenDetailActivity(val activity: TripBodyMiddleware) : HomeEvent()
    data class OpenUserProfile(val userId: Int) : HomeEvent()
    object OpenProfileScreen: HomeEvent()
    object OpenEventScreen: HomeEvent()
    object OpenGroupScreen: HomeEvent()
    object OpenFriendsEvent: HomeEvent()
    object OpenTripScreen: HomeEvent()
    data class Follow(val userId: Int) : HomeEvent()
    data class Unfollow(val userId: Int) : HomeEvent()

    data class OpenEventDetail(
        val event: EventData,
    ) : HomeEvent()
    data class JoinEvent(val eventId: Int) : HomeEvent()
    data class LeaveEvent(val eventId: Int) : HomeEvent()
    object ClearRecentlyJoined : HomeEvent()
}