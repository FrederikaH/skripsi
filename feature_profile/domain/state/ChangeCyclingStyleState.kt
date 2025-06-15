package com.polygonbikes.ebike.v3.feature_profile.domain.state

import kotlinx.serialization.Serializable

@Serializable
data class ChangeCyclingStyleState (
    val isLoading: Boolean = false,
    val newCyclingStyle: List<String> = listOf<String>()
)