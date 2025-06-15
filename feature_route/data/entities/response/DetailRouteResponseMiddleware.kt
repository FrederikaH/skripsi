package com.polygonbikes.ebike.v3.feature_route.data.entities.response

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class DetailRouteResponseMiddleware (
    @com.squareup.moshi.Json(name = "data")
    var data: RouteData
)