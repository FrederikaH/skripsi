package com.polygonbikes.ebike.v3.feature_route.data.entities.response

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
@Serializable
@Parcelize
data class RoadTypeData (
    @field:Json(name = "paved_road")
    var paved: Int? = null,

    @field:Json(name = "gravel_road")
    var gravel: Int? = null,

    @field:Json(name = "off_road")
    var offroad: Int? = null
) : Parcelable