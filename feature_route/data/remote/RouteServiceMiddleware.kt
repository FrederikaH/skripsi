package com.polygonbikes.ebike.v3.feature_route.data.remote

import com.polygonbikes.ebike.v3.feature_route.data.entities.response.DetailRouteResponseMiddleware
import com.polygonbikes.ebike.core.model.MessageData
import com.polygonbikes.ebike.v3.feature_route.data.entities.body.SaveRouteBody
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.ListRouteResponseMiddleware
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.core.entities.response.CommentResponse
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RoadTypeCountResponseMiddleware
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RouteServiceMiddleware {
    @Multipart
    @POST("api/v1/route")
    fun createRoute(
        @Part("name") name: RequestBody,
        @Part("location_id") locationId: RequestBody,
        @Part("start_latitude") startLatitude: RequestBody,
        @Part("start_longitude") startLongitude: RequestBody,
        @Part("road_type[]") roadType: List<@JvmSuppressWildcards RequestBody>,
        @Part("distance") distance: RequestBody,
        @Part("elevation") elevation: RequestBody,
        @Part("purpose") purpose: RequestBody,
        @Part("polyline") polyline: RequestBody,
        @Part file: MultipartBody.Part?,
        @Part imageFiles: List<MultipartBody.Part>
    ): Call<DetailRouteResponseMiddleware>

    @Multipart
    @POST("api/v1/route/{route_id}/update")
    fun editRoute(
        @Path("route_id") routeId: Int,
        @Part("name") name: RequestBody,
        @Part("location_id") locationId: RequestBody,
        @Part("start_latitude") startLatitude: RequestBody,
        @Part("start_longitude") startLongitude: RequestBody,
        @Part("road_type[]") roadType: List<@JvmSuppressWildcards RequestBody>,
        @Part("distance") distance: RequestBody,
        @Part("elevation") elevation: RequestBody,
        @Part("purpose") purpose: RequestBody,
        @Part("polyline") polyline: RequestBody,
        @Part file: MultipartBody.Part?,
        @Part images: List<MultipartBody.Part>,
        @Part("old_image_ids[]") oldImageIds: List<@JvmSuppressWildcards RequestBody>
    ): Call<DetailRouteResponseMiddleware>

    @Headers("Content-Type: application/json")
    @GET("api/v1/route")
    fun getListRoute(
        @Query("location[latitude]") locationLatitude: Double? = null,
        @Query("location[longitude]") locationLongitude: Double? = null,
        @Query("location[radius]") locationRadius: Double? = null,
        @Query("purpose") purpose: String? = null,
        @Query("roads") roads: String? = null,
        @Query("distance[from]") distanceFrom: Double? = null,
        @Query("distance[to]") distanceTo: Double? = null,
        @Query("elevation[from]") elevationFrom: Int? = null,
        @Query("elevation[to]") elevationTo: Int? = null,
        @Query("cities") cities: String? = null,
        @Query("recommended") recommended: Boolean? = null,
        @Query("friends") friends: Boolean? = null,
        @Query("limit") limit: Int? = null,
        @Query("official") official: Int? = null,
        @Query("deleted") deleted: Boolean? = null
    ) : Call<ListRouteResponseMiddleware>

    @Headers("Content-Type: application/json")
    @GET("api/v1/route/bookmark")
    fun getListSavedRoute() : Call<ListRouteResponseMiddleware>

    @Headers("Content-Type: application/json")
    @GET("api/v1/route/mostSaved")
    fun getListMostSavedRoute() : Call<ListRouteResponseMiddleware>

    @Headers("Content-Type: application/json")
    @GET("api/v1/route/roadTypesCount")
    fun getRoadTypesCount(
        @Query("period") period: String
    ) : Call<RoadTypeCountResponseMiddleware>

    @Headers("Content-Type: application/json")
    @POST("api/v1/route/{route_id}/add-bookmark")
    fun saveRoute(
        @Path("route_id") routeId: Int,
        @Body body: SaveRouteBody
    ): Call<RouteData>

    @Headers("Content-Type: application/json")
    @GET("api/v1/route/{route_id}")
    fun getDetailRoute(
        @Path("route_id") routeId: Int,
        @Query("embed") embed: String? = null
    ) : Call<DetailRouteResponseMiddleware>

    @Headers("Content-Type: application/json")
    @POST("api/v1/route/{route_id}/add-comment")
    fun addComment (
        @Path("route_id") routeId: Int,
        @Body message: MessageData
    ) : Call<CommentResponse>

    @Headers("Content-Type: application/json")
    @DELETE("api/v1/route/{route_id}")
    fun deleteRoute(
        @Path("route_id") routeId: Int
    ): Call<RouteData>

    @Headers("Content-Type: application/json")
    @POST("api/v1/route/{route_id}/restore")
    fun restoreRoute(
        @Path("route_id") routeId: Int
    ): Call<RouteData>
}