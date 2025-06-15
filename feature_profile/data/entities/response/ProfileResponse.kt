package com.polygonbikes.ebike.v3.feature_profile.data.entities.response

import android.os.Parcelable
import com.polygonbikes.ebike.core.model.LocationData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
data class ProfileResponse (

    @field:Json(name = "data")
    val data: ProfileData?
)

@JsonClass(generateAdapter = true)
@Parcelize
@Serializable
data class ProfileData (

    @field:Json(name = "id")
    var userId: Int? = null,

    @field:Json(name = "username")
    var username: String? = null,

    @field:Json(name = "email")
    var email: String? = null,

    @field:Json(name = "mobile_phone")
    var mobilePhone: String? = null,

    @field:Json(name = "cycling_style")
    var cyclingStyle: List<String>? = listOf<String>(),

    @field:Json(name = "location")
    var city: LocationData? = null,

    @field:Json(name = "city")
    var location: LocationData? = null,

    @field:Json(name = "photo")
    var photo: Photo? = null,

    @field:Json(name = "is_followed")
    var isFollowed: Boolean? = false,

    @field:Json(name = "total_follower")
    var totalFollower: Int? = 0,

    @field:Json(name = "total_following")
    var totalFollowing: Int? = 0
) : Parcelable


@JsonClass(generateAdapter = true)
@Serializable
@Parcelize
data class Photo(
    @field:Json(name = "url")
    var url: String? = null,

    @field:Json(name = "type")
    var type: String? = null,
) : Parcelable