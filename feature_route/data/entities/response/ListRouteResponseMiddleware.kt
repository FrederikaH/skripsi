package com.polygonbikes.ebike.v3.feature_route.data.entities.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListRouteResponseMiddleware(
    @Json(name = "data")
    var data: List<RouteData>
)