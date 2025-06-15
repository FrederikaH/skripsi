package com.polygonbikes.ebike.v3.feature_profile.util

import com.polygonbikes.ebike.v3.feature_profile.domain.model.UserProfileMiddleware
import kotlinx.coroutines.flow.Flow

interface ProfileMiddlewareManager {
    val userProfileFlow: Flow<UserProfileMiddleware>
    suspend fun userId(): Long?
    suspend fun updateData(userId: Long? = null, username: String? = null, location: String? = null, cyclingStyle: List<String>? = null, email: String? = null, phoneNumber: String? = null)
    suspend fun clearData()
}