package com.polygonbikes.ebike.v3.feature_profile.domain.model

data class UserProfileMiddleware (
    var userId: Long?,
    var username: String,
    var location: String,
    var cyclingStyle: List<String>,
    var email: String,
    var phoneNumber: String
)