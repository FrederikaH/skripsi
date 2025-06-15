package com.polygonbikes.ebike.v3.feature_profile.data.entities.body

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FindUserBody (
    @field:Json(name = "name")
    var name: String? = null
)