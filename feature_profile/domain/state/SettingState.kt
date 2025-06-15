package com.polygonbikes.ebike.v3.feature_profile.domain.state

import com.polygonbikes.ebike.core.enums.BafangServiceStatus
import kotlinx.coroutines.flow.MutableStateFlow

data class SettingState (
    val userStravaName: String? = null,
    val isLoggedInStrava: Boolean = false,
    val isProcessingLogout: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val bafangStatus: MutableStateFlow<BafangServiceStatus?> = MutableStateFlow(null),
)