package com.polygonbikes.ebike.v3.feature_profile.domain.state

data class ChangeLocationState (
    val isLoading: Boolean = false,
    val newLocation: String = ""
)