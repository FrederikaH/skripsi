package com.polygonbikes.ebike.v3.feature_route.data.entities.response

import android.os.Parcelable
import com.polygonbikes.ebike.core.entities.FileEntity
import com.polygonbikes.ebike.core.model.CommentData
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.model.Images
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
@Serializable
@Parcelize
data class RouteData (

    @field:Json(name = "id")
    var id: Int = 0,

    @field:Json(name = "name")
    var name: String? = null,

    @field:Json(name = "thumbnail")
    var thumbnail: FileEntity? = null,

    @field:Json(name = "images")
    var images: List<Images>? = null,

    @field:Json(name = "start_location")
    var startLocationData: LocationData? = null,

    @field:Json(name = "start_latitude")
    var startLatitude: Float? = null,

    @field:Json(name = "start_longitude")
    var startLongitude: Float? = null,

    @field:Json(name = "road_type")
    var roadType: List<String>? = null,

    @field:Json(name = "distance")
    var distance: Double? = null,

    @field:Json(name = "elevation")
    var elevation: Int? = null,

    @field:Json(name = "purpose")
    var purpose: String? = null,

    @field:Json(name = "polyline")
    var polyline: String? = null,

    @field:Json(name = "gpx")
    var gpx: FileEntity? = null,

    @field:Json(name = "comments")
    var comments: List<CommentData?> = listOf<CommentData>(),

    @field:Json(name = "total_comments")
    var totalComments: Int? = null,

    @field:Json(name = "total_bookmark")
    var totalBookmark: Int? = null,

    @field:Json(name = "create_at")
    var createAt: String? = null,

    @field:Json(name = "update_at")
    var updateAt: String? = null,

    @field:Json(name = "deleted_at")
    var deletedAt: String? = null

) : Parcelable