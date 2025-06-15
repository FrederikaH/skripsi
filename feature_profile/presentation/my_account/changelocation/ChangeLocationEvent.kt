package com.polygonbikes.ebike.v3.feature_profile.presentation.my_account.changelocation

sealed class ChangeLocationEvent {
    data class InputChangeLocation(val value: String) : ChangeLocationEvent()
    object ChangeLocation : ChangeLocationEvent()
}