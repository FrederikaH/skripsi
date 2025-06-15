package com.polygonbikes.ebike.v3.feature_profile.presentation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.navOptions
import com.polygonbikes.ebike.core.dao.LogExerciseDao
import com.polygonbikes.ebike.core.database.LogDatabase
import com.polygonbikes.ebike.core.route.DetailEventRoute
import com.polygonbikes.ebike.core.route.HistoryRoute
import com.polygonbikes.ebike.core.route.LoginRoute
import com.polygonbikes.ebike.core.route.MainRoute
import com.polygonbikes.ebike.core.route.RouteManagementRoute
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.*
import com.polygonbikes.ebike.core.util.tokenmanager.TokenManager
import com.polygonbikes.ebike.v3.feature_event.data.entities.response.EventListResponseMiddleware
import com.polygonbikes.ebike.v3.feature_event.data.remote.EventServiceMiddleware
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileResponse
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.TripSummaryResponse
import com.polygonbikes.ebike.v3.feature_profile.data.remote.ProfileServiceMiddleware
import com.polygonbikes.ebike.v3.feature_profile.domain.state.ProfileState
import com.polygonbikes.ebike.v3.feature_profile.util.ProfileMiddlewareManager
import com.polygonbikes.ebike.v3.feature_trip.data.remote.TripServiceMiddleware
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val profileServiceMiddleware: ProfileServiceMiddleware,
    private val tripServiceMiddleware: TripServiceMiddleware,
    private val profileMiddlewareManager: ProfileMiddlewareManager,
    val eventServiceMiddleware: EventServiceMiddleware,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val TAG = "ProfileVM"
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> get() = _state

    private val roomDB: LogDatabase = LogDatabase.getDatabase(context)
    private val logExercise: LogExerciseDao = roomDB.logExerciseDao()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.FetchData -> {
                getProfileData(state.value.period.toString())
                getUpcomingEventData()
            }

            is ProfileEvent.OpenHistoryScreen -> {
                sendUiEvent(Navigate(route = HistoryRoute))
            }

            is ProfileEvent.OpenRouteManagementScreen -> {
                sendUiEvent(Navigate(route = RouteManagementRoute))
            }

            is ProfileEvent.SetValuePeriod -> {
                _state.value = state.value.copy(period = event.value)
                getProfileData(event.value)
            }

            is ProfileEvent.OpenEventDetail -> {
                _state.value = state.value.copy(isLoading = true)
                sendUiEvent(
                    Navigate(
                        route = DetailEventRoute(
                            event = event.event
                        )
                    )
                )
            }
        }
    }

    private fun getProfileData(period: String) {
        _state.value = _state.value.copy(isProfileLoading = true)
        _state.value = _state.value.copy(isTripSummaryLoading = true)

        val profileCall: Call<ProfileResponse> = profileServiceMiddleware.getOwnProfile()
        val tripSummaryCall: Call<TripSummaryResponse> = tripServiceMiddleware.getTripSummary(period, state.value.profile?.userId)

        profileCall.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                Log.d(TAG, "API Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "API Response Body: $responseBody")
                    val profileData = response.body()?.data
                    if (profileData != null) {
                        _state.value = _state.value.copy(profile = profileData)
                    } else {
                        _state.value = _state.value.copy(errorMessage = "Failed to load profile")
                    }
                } else {
                    _state.value = _state.value.copy(errorMessage = "Failed to load profile")
                    Log.e(TAG, "API Error: ${response.errorBody()?.string()}")

                    when (response.code()) {
                        401 -> {
                            viewModelScope.launch {
                                tokenManager.deleteToken()
                                profileMiddlewareManager.clearData()
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

                _state.value = _state.value.copy(isProfileLoading = false)
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                _state.value = _state.value.copy(isProfileLoading = false, errorMessage = "Network error: ${t.message}")
            }
        })

        tripSummaryCall.enqueue(object : Callback<TripSummaryResponse> {
            override fun onResponse(call: Call<TripSummaryResponse>, response: Response<TripSummaryResponse>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { data ->
                        _state.value = _state.value.copy(tripSummary = data)
                    }

                } else {
                    _state.value = _state.value.copy(errorMessage = "Failed to load trip summary")
                }
                _state.value = _state.value.copy(isTripSummaryLoading = false)
            }

            override fun onFailure(call: Call<TripSummaryResponse>, t: Throwable) {
                _state.value = _state.value.copy(isTripSummaryLoading = false, errorMessage = "Network error: ${t.message}")
            }
        })
    }

    private fun getUpcomingEventData() {
        _state.value = state.value.copy(isUpcomingEventLoading = true)
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = dateFormat.format(cal.time)

        eventServiceMiddleware.getListEvent(
            participating = 1,
            startFrom = todayDate
        ).enqueue(object : Callback<EventListResponseMiddleware> {
            override fun onResponse(
                call: Call<EventListResponseMiddleware>,
                response: Response<EventListResponseMiddleware>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { data ->
                        _state.value.listUpcomingEvent.apply {
                            clear()
                            addAll(data.reversed())
                        }
                    }
                } else {
                    Log.e(TAG, "Joined Events Error: ${response.errorBody()?.string()}")
                }
                _state.value = state.value.copy(isUpcomingEventLoading = false)
            }

            override fun onFailure(call: Call<EventListResponseMiddleware>, t: Throwable) {
                _state.value = state.value.copy(isUpcomingEventLoading = false)
                Log.e(TAG, "Joined Events Failure: ${t.message}")
            }
        })
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch { _uiEvent.send(event) }
    }
}
