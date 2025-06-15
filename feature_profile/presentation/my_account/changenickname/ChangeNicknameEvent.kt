package com.polygonbikes.ebike.v3.feature_profile.presentation.my_account.changenickname

sealed class ChangeNicknameEvent {
    data class InputChangeNickname(val value: String) : ChangeNicknameEvent()
    object ChangeNickname : ChangeNicknameEvent()
}