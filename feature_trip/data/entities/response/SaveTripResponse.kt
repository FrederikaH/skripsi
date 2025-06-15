package com.polygonbikes.ebike.v3.feature_trip.data.entities.response

import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class SaveTripResponse (
    @field:Json(name = "data")
    var data: TripBodyMiddleware? = null
)