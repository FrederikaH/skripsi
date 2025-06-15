package com.polygonbikes.ebike.v3.feature_profile.data.entities.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TripSummaryResponse (
    @field:Json(name = "data")
    var data: TripSummaryData? = null
)

@JsonClass(generateAdapter = true)
data class TripSummaryData (

    @field:Json(name = "distance")
    var distance: Double? = 0.0,

    @field:Json(name = "elevation")
    var elevation: Int? = 0,

    @field:Json(name = "time")
    var time: Int? = 0,

    @field:Json(name = "trips")
    var trips: Int? = 0
)