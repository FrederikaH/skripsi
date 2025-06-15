package com.polygonbikes.ebike.v3.feature_profile.data.entities.body

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfileBodyMiddleware(
    @field:Json(name = "username")
    var username: String? = null,

    @field:Json(name = "location")
    var location: String? = null,

    @field:Json(name = "cycling_style")
    var cyclingStyle: List<String>? = null
)