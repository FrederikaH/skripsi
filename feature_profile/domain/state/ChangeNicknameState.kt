package com.polygonbikes.ebike.v3.feature_profile.domain.state

data class ChangeNicknameState (
    val isLoading: Boolean = false,
    val newNickname: String = ""
)