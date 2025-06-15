package com.polygonbikes.ebike.v3.feature_trip.data.remote

import com.polygonbikes.ebike.feature_strava.domain.entities.body.UpdateActivityIDBody
import com.polygonbikes.ebike.feature_strava.domain.entities.body.UpdateUploadIDBody
import com.polygonbikes.ebike.feature_trip.domain.entities.body.UpdateStatusBody
import com.polygonbikes.ebike.v3.feature_home.data.entities.DetailTripBodyMiddleware
import com.polygonbikes.ebike.core.model.MessageData
import com.polygonbikes.ebike.v3.feature_trip.data.entities.ListTripBodyMiddleware
import com.polygonbikes.ebike.core.entities.response.CommentResponse
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.TripSummaryResponse
import com.polygonbikes.ebike.v3.feature_trip.data.entities.response.SaveTripResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TripServiceMiddleware {
    @Multipart
    @POST("api/v1/trip")
    fun saveTrip(
        @Part fitFile: MultipartBody.Part,
        @Part imageFiles: List<MultipartBody.Part>,
        @Part gpxFile: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("timestamp") timestamp: RequestBody,
        @Part("moving_time") movingTime: RequestBody,
        @Part("elapsed_time") elapsedTime: RequestBody,
        @Part("avg_speed") avgSpeed: RequestBody,
        @Part("max_speed") maxSpeed: RequestBody,
        @Part("bike_name") bikeName: RequestBody,
        @Part("start_location") startLocation: RequestBody,
        @Part("start_latitude") startLatitude: RequestBody,
        @Part("start_longitude") startLongitude: RequestBody,
        @Part("road_type[]") roadType: List<@JvmSuppressWildcards RequestBody>,
        @Part("distance") distance: RequestBody,
        @Part("elevation") elevation: RequestBody,
        @Part("purpose") purpose: RequestBody,
        @Part("polyline") polyline: RequestBody,
        @Part("is_private") isPrivate: RequestBody
    ): Call<SaveTripResponse>

    @Headers("Content-Type: application/json")
    @GET("api/v1/trip")
    fun getListTrip(
        @Query("period") period: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("embed") embed: String? = null,
        @Query("user_id") userId: Int? = null
    ): Call<ListTripBodyMiddleware>

    @Headers("Content-Type: application/json")
    @GET("api/v1/trip/{trip_id}")
    fun getDetailTrip(
        @Path("trip_id") tripId: Int,
        @Query("embed") embed: String? = null
    ): Call<DetailTripBodyMiddleware>

    @Headers("Content-Type: application/json")
    @POST("api/v1/strava/update-upload-id")
    fun updateUploadID(@Body body: UpdateUploadIDBody): Call<DetailTripBodyMiddleware>

    @Headers("Content-Type: application/json")
    @POST("api/v1/strava/update-activity-id")
    fun updateActivityID(@Body body: UpdateActivityIDBody): Call<DetailTripBodyMiddleware>

    @Headers("Content-Type: application/json")
    @POST("api/v1/strava/update-status")
    fun updateStatusStrava(@Body body: UpdateStatusBody): Call<DetailTripBodyMiddleware>

    @Headers("Content-Type: application/json")
    @POST("api/v1/trip/{trip_id}/add-comment")
    fun addComment(
        @Path("trip_id") tripId: Int,
        @Body message: MessageData
    ): Call<CommentResponse>

    @Headers("Content-Type: application/json")
    @GET("api/v1/trip")
    fun getListActivity(
        @Query("period") period: String? = "all",
        @Query("limit") limit: Int? = 20,
        @Query("embed") embed: String? = "user.photo",
        @Query("user_id") userId: Long? = null
    ): Call<ListTripBodyMiddleware>

    @Headers("Content-Type: application/json")
    @GET("api/v1/trip/summary")
    fun getTripSummary(
        @Query("period") period: String,
        @Query("user_id") userId: Int? = null
    ): Call<TripSummaryResponse>

}