package com.polygonbikes.ebike.v3.feature_profile.presentation

import com.polygonbikes.ebike.v3.feature_event.data.entities.EventData

sealed class ProfileEvent {
    object FetchData : ProfileEvent()
    data class SetValuePeriod(val value: String) : ProfileEvent()
    object OpenHistoryScreen: ProfileEvent()
    object OpenRouteManagementScreen: ProfileEvent()
    data class OpenEventDetail(
        val event: EventData,
    ) : ProfileEvent()
//    object OpenSavedRouteScreen: ProfileEvent()
}