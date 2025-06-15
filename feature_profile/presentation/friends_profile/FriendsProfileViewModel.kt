package com.polygonbikes.ebike.v3.feature_profile.presentation.friends_profile

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.navOptions
import com.polygonbikes.ebike.core.dao.LogExerciseDao
import com.polygonbikes.ebike.core.database.LogDatabase
import com.polygonbikes.ebike.core.route.DetailActivityRoute
import com.polygonbikes.ebike.core.route.LoginRoute
import com.polygonbikes.ebike.core.route.MainRoute
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.Navigate
import com.polygonbikes.ebike.core.util.tokenmanager.TokenManager
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileResponse
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.TripSummaryResponse
import com.polygonbikes.ebike.v3.feature_profile.data.remote.ProfileServiceMiddleware
import com.polygonbikes.ebike.v3.feature_profile.domain.state.FriendsProfileState
import com.polygonbikes.ebike.v3.feature_profile.util.ProfileMiddlewareManager
import com.polygonbikes.ebike.v3.feature_trip.data.entities.ListTripBodyMiddleware
import com.polygonbikes.ebike.v3.feature_trip.data.remote.TripServiceMiddleware
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class FriendsProfileViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val profileServiceMiddleware: ProfileServiceMiddleware,
    private val tripServiceMiddleware: TripServiceMiddleware,
    private val tokenManager: TokenManager,
    private val profileMiddlewareManager: ProfileMiddlewareManager
) : ViewModel() {

    private val TAG = "FriendsProfileVM"
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var _state = mutableStateOf(FriendsProfileState())
    val state: State<FriendsProfileState> get() = _state

    private val roomDB: LogDatabase = LogDatabase.getDatabase(context)
    private val logExercise: LogExerciseDao = roomDB.logExerciseDao()

    fun onEvent(event: FriendsProfileEvent) {
        when (event) {
            is FriendsProfileEvent.GetUserProfile -> {
                _state.value = state.value.copy(userId = event.userId)
                getUserProfileData(event.userId)

                viewModelScope.launch {
                    val loggedUserId = profileMiddlewareManager.userId()

                    _state.value = state.value.copy(
                        isOthersProfile = state.value.userId != loggedUserId?.toInt()
                    )
                }
            }

            is FriendsProfileEvent.GetTripSummary -> {
                _state.value = state.value.copy(period = event.period)
                _state.value = state.value.copy(userId = event.userId)
                getTripSummary(event.period, event.userId)
            }

            is FriendsProfileEvent.Follow -> {
                if (state.value.profile?.isFollowed == true) return

                _state.value = state.value.copy(isLoading = true)

                val call: Call<ProfileResponse> = profileServiceMiddleware.follow(
                    userId = state.value.userId
                )

                call.enqueue(object : Callback<ProfileResponse> {
                    override fun onResponse(
                        call: Call<ProfileResponse>,
                        response: Response<ProfileResponse>
                    ) {
                        _state.value = state.value.copy(isLoading = false)

                        if (response.isSuccessful) {
                            state.value.profile?.isFollowed = true
                            getUserProfileData(state.value.userId)
                        }
                    }

                    override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                        Log.e(TAG, "onFailure: ${t.message}")
                        _state.value = state.value.copy(isLoading = false)
                    }
                })
            }

            is FriendsProfileEvent.Unfollow -> {
                if (state.value.profile?.isFollowed == false) return

                _state.value = state.value.copy(isLoading = true)

                val call: Call<ProfileResponse> = profileServiceMiddleware.unfollow(
                    userId = state.value.userId
                )

                call.enqueue(object : Callback<ProfileResponse> {
                    override fun onResponse(
                        call: Call<ProfileResponse>,
                        response: Response<ProfileResponse>
                    ) {
                        _state.value = state.value.copy(isLoading = false)

                        if (response.isSuccessful) {
                            state.value.profile?.isFollowed = false
                            getUserProfileData(state.value.userId)
                        }
                    }

                    override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                        Log.e(TAG, "onFailure: ${t.message}")
                        _state.value = state.value.copy(isLoading = false)
                    }
                })
            }

            is FriendsProfileEvent.GetUserActivity -> {
                _state.value = state.value.copy(userId = event.userId)
                getUserActivity(event.userId)
            }

            is FriendsProfileEvent.OpenDetailActivity -> sendUiEvent(UiEvent.Navigate(route = DetailActivityRoute(activity = event.activity)))
        }
    }

    private fun getUserActivity(userId: Int){
        _state.value = state.value.copy(isLoading = true)

        val tripCall: Call<ListTripBodyMiddleware> = tripServiceMiddleware.getListTrip(userId = userId)
        tripCall.enqueue(object : Callback<ListTripBodyMiddleware> {
            override fun onResponse(
                call: Call<ListTripBodyMiddleware>,
                response: Response<ListTripBodyMiddleware>
            ) {
                Log.d(TAG, "Trip API Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "Trip API Response Body: $responseBody")

                    responseBody?.data?.let { data ->
                        Log.d(TAG, "Received trip data: $data")

                        _state.value.listActivity.clear()
                        _state.value.listActivity.addAll(data)

                        Log.d(TAG, "Updated listActivity: ${_state.value.listActivity}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Trip API Error: $errorBody")

                    when (response.code()) {
                        401 -> {
                            tokenManager.deleteToken()
                            sendUiEvent(
                                Navigate(
                                    route = LoginRoute(forceLogout = true, reLogin = true),
                                    navOptions = navOptions {
                                        popUpTo<MainRoute> {
                                            inclusive = true
                                        }
                                    })
                            )
                        }
                    }
                }
                _state.value = state.value.copy(isLoading = false)
            }

            override fun onFailure(call: Call<ListTripBodyMiddleware>, t: Throwable) {
                Log.e(TAG, "Trip API Failure: ${t.message}")
                _state.value = state.value.copy(isLoading = false)
            }
        })
    }

    private fun getUserProfileData(userId: Int) {
        _state.value = _state.value.copy(isLoading = true)
        val UsersProfileCall: Call<ProfileResponse> = profileServiceMiddleware.getUserProfile(userId)

        UsersProfileCall.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(
                call: Call<ProfileResponse>,
                response: Response<ProfileResponse>
            ) {
                Log.d(TAG, "User Profile API Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "API Response Body: $responseBody")
                    val profileData = response.body()?.data
                    _state.value = _state.value.copy(profile = profileData, isLoading = false)
                } else {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = "Failed to load profile")
                    Log.e(TAG, "API Error: ${response.errorBody()?.string()}")

                    when (response.code()) {
                        401 -> {
                            tokenManager.deleteToken()
                            sendUiEvent(
                                Navigate(
                                    route = LoginRoute(forceLogout = true, reLogin = true),
                                    navOptions = navOptions {
                                        popUpTo<MainRoute> {
                                            inclusive = true
                                        }
                                    })
                            )
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Network error: ${t.message}")
            }
        })
    }

    private fun getTripSummary(period: String, userId: Int) {

        val tripSummaryCall: Call<TripSummaryResponse> = tripServiceMiddleware.getTripSummary(period, userId)

        tripSummaryCall.enqueue(object : Callback<TripSummaryResponse> {
            override fun onResponse(call: Call<TripSummaryResponse>, response: Response<TripSummaryResponse>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { data ->
                        _state.value = _state.value.copy(tripSummary = data, isActivityLoading = false)
                    }

                } else {
                    _state.value = _state.value.copy(isActivityLoading = false, errorMessage = "Failed to load trip summary")
                }
            }

            override fun onFailure(call: Call<TripSummaryResponse>, t: Throwable) {
                _state.value = _state.value.copy(isActivityLoading = false, errorMessage = "Network error: ${t.message}")
            }
        })
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch { _uiEvent.send(event) }
    }
}