package com.polygonbikes.ebike.v3.feature_profile.data.entities.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListUserResponseMiddleware (
    @Json(name = "data")
    var data: List<ProfileData>
)