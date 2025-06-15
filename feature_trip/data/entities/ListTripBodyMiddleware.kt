package com.polygonbikes.ebike.v3.feature_trip.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListTripBodyMiddleware(
    @Json(name = "data")
    var data: List<TripBodyMiddleware>
)