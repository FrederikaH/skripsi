package com.polygonbikes.ebike.v3.feature_profile.presentation.setting

import com.polygonbikes.ebike.feature_me.presentation.MeEvent
import com.polygonbikes.ebike.v3.feature_profile.presentation.ProfileEvent

sealed class SettingEvent {
    object OpenMyAccountScreen: SettingEvent()
    object OnInitScreen : SettingEvent()
    object OpenStrava : SettingEvent()
    data class SetLanguage(val languageKey: String) : SettingEvent()
    object OpenDetailStrava : SettingEvent()
    object ShowDialogDisconnectStrava : SettingEvent()
    object DisconnectStrava : SettingEvent()
    object Logout : SettingEvent()
}