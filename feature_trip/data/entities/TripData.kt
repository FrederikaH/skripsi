package com.polygonbikes.ebike.v3.feature_trip.data.entities

import android.os.Parcelable
import com.polygonbikes.ebike.core.entities.FileEntity
import com.polygonbikes.ebike.core.model.CommentData
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileData
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
@Serializable
@Parcelize
data class TripBodyMiddleware (

    @field:Json(name = "id")
    var id: Int = 0,

    @field:Json(name = "timestamp")
    var timestamp: Long? = 0,

    @field:Json(name = "user")
    var user: ProfileData? = null,

    @field:Json(name = "name")
    var name: String? = null,

    @field:Json(name = "date")
    var date: String? = null,

    @field:Json(name = "moving_time")
    var movingTime: Long? = null,

    @field:Json(name = "elapsed_time")
    var elapsedTime: Long? = null,

    @field:Json(name = "avg_speed")
    var avgSpeed: Double? = null,

    @field:Json(name = "max_speed")
    var maxSpeed: Double? = null,

    @field:Json(name = "bike_name")
    var bikeName: String? = null,

    @field:Json(name = "thumbnail")
    var thumbnail: FileEntity? = null,

    @field:Json(name = "route")
    var route: RouteData? = null,

    @field:Json(name = "fit")
    var fit: FileEntity? = null,

    @field:Json(name = "strava")
    var strava: Strava? = null,

    @field:Json(name = "comments")
    var comments: List<CommentData?> = listOf<CommentData>(),

    @field:Json(name = "total_comments")
    var totalComments: Int? = null,

    @field:Json(name = "is_private")
    var isPrivate: Int? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Serializable
@Parcelize
data class Strava(
    @field:Json(name = "status")
    val status: String? = null,

    @field:Json(name = "upload_id")
    val upload_id: Long? = null,

    @field:Json(name = "activity_id")
    val activity_id: Long? = null

) : Parcelable