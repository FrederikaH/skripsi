package com.polygonbikes.ebike.v3.feature_profile.presentation.my_account.changecyclingstyle

sealed class ChangeCyclingStyleEvent {
    data class InputChangeCyclingStyle(val value: List<String>) : ChangeCyclingStyleEvent()
    object ChangeCyclingStyle : ChangeCyclingStyleEvent()
}