package com.polygonbikes.ebike.v3.feature_trip.data.entities.body

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SaveTripBody(
//    @field:Json(name = "fit")
//    var fit: ?,

    @field:Json(name = "name")
    var name: String,

    @field:Json(name = "moving_time")
    var movingTime: String,

    @field:Json(name = "elapsed_time")
    var elapsedTime: String,

    @field:Json(name = "avg_speed")
    var avgSpeed: String,

    @field:Json(name = "max_speed")
    var maxSpeed: String,

    @field:Json(name = "bike_name")
    var bikeName: String,

    @field:Json(name = "start_location")
    var startLocation: String,

    @field:Json(name = "start_latitude")
    var startLatitude: String,

    @field:Json(name = "start_longitude")
    var startLongitude: String,

    @field:Json(name = "road_type")
    var roadType: List<String>,

    @field:Json(name = "distance")
    var distance: Double,

    @field:Json(name = "elevation")
    var elevation: Int,

    @field:Json(name = "purpose")
    var purpose: String,

    @field:Json(name = "polyline")
    var polyline: String

//    @field:Json(name = "privacy")
//    var privacy: String,    // String / boolean?
)
