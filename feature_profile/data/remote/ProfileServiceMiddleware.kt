package com.polygonbikes.ebike.v3.feature_profile.data.remote

import com.polygonbikes.ebike.v3.feature_profile.data.entities.body.EditProfileBodyMiddleware
import com.polygonbikes.ebike.v3.feature_profile.data.entities.body.FindUserBody
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ListUserResponseMiddleware
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileServiceMiddleware {

    @Headers("Content-Type: application/json")
    @GET("api/v1/user")
    fun getOwnProfile(
        @Query("embed") embed: String = "photo,location"
    ): Call<ProfileResponse>

    @Headers("Content-Type: application/json")
    @GET("api/v1/user/{user_Id}")
    fun getUserProfile(
        @Path("user_Id") userId: Int,
        @Query("embed") embed: String? = "photo,location"
    ): Call<ProfileResponse>

    @Headers("Content-Type: application/json")
    @DELETE("api/v1/user")
    fun deleteProfile(): Call<ProfileResponse>

    @PUT("api/v1/user")
    @Headers("Content-Type: application/json")
    fun editProfile(@Body body: EditProfileBodyMiddleware) : Call<ProfileResponse>

    @Headers("Content-Type: application/json")
    @POST("api/v1/user/{user_Id}/follow")
    fun follow(@Path("user_Id") userId: Int): Call<ProfileResponse>

    @Headers("Content-Type: application/json")
    @POST("api/v1/user/{user_Id}/unfollow")
    fun unfollow(@Path("user_Id") userId: Int): Call<ProfileResponse>

    @Headers("Content-Type: application/json")
    @POST("api/v1/user/find")
    fun getListUser(
        @Body body: FindUserBody,
        @Query("cities") cities: String? = null,
        @Query("types") types: String? = null,
        @Query("embed") embed: String? = "photo,location",
    ) : Call<ListUserResponseMiddleware>

    @Headers("Content-Type: application/json")
    @GET("api/v1/user/suggest")
    fun getUserSuggestions(
        @Query("embed") embed: String? = "photo,location"
    ) : Call<ListUserResponseMiddleware>
}