package com.polygonbikes.ebike.v3.feature_home.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.navOptions
import com.polygonbikes.ebike.core.FeatureList
import com.polygonbikes.ebike.core.route.DetailActivityRoute
import com.polygonbikes.ebike.core.route.DetailEventRoute
import com.polygonbikes.ebike.core.route.EventRoute
import com.polygonbikes.ebike.core.route.FriendsEventRoute
import com.polygonbikes.ebike.core.route.FriendsProfileRoute
import com.polygonbikes.ebike.core.route.GroupRoute
import com.polygonbikes.ebike.core.route.LoginRoute
import com.polygonbikes.ebike.core.route.MainRoute
import com.polygonbikes.ebike.core.route.ProfileRoute
import com.polygonbikes.ebike.core.route.TripRoute
import com.polygonbikes.ebike.core.util.TimeUtil
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.Navigate
import com.polygonbikes.ebike.core.util.tokenmanager.TokenManager
import com.polygonbikes.ebike.feature_main.data.BottomBarData
import com.polygonbikes.ebike.v3.feature_event.data.entities.response.EventListResponseMiddleware
import com.polygonbikes.ebike.v3.feature_event.data.entities.response.JoinEventResponse
import com.polygonbikes.ebike.v3.feature_event.data.remote.EventServiceMiddleware
import com.polygonbikes.ebike.v3.feature_home.data.entities.response.ListWeatherResponse
import com.polygonbikes.ebike.v3.feature_home.data.remote.WeatherServiceMiddleware
import com.polygonbikes.ebike.v3.feature_home.domain.state.HomeState
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileResponse
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.TripSummaryResponse
import com.polygonbikes.ebike.v3.feature_profile.data.remote.ProfileServiceMiddleware
import com.polygonbikes.ebike.v3.feature_profile.util.ProfileMiddlewareManager
import com.polygonbikes.ebike.v3.feature_trip.data.entities.ListTripBodyMiddleware
import com.polygonbikes.ebike.v3.feature_trip.data.remote.TripServiceMiddleware
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val profileServiceMiddleware: ProfileServiceMiddleware,
    private val weatherServiceMiddleware: WeatherServiceMiddleware,
    private val tripServiceMiddleware: TripServiceMiddleware,
    private val tokenManager: TokenManager,
    private val eventServiceMiddleware: EventServiceMiddleware,
    private val profileMiddlewareManager: ProfileMiddlewareManager
) : ViewModel() {
    private val TAG = "HomeVM"
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var _state: MutableState<HomeState> = mutableStateOf(HomeState())
    val state: State<HomeState> get() = _state

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OpenProfileScreen -> {
                sendUiEvent(
                    Navigate(
                        route = MainRoute(target = BottomBarData.Profile.route),
                        navOptions = navOptions { popUpTo<ProfileRoute> { inclusive = true } }
                    )
                )
            }

            is HomeEvent.OpenTripScreen -> sendUiEvent(Navigate(route = TripRoute(code = null)))
            is HomeEvent.OpenEventScreen -> sendUiEvent(Navigate(route = EventRoute))
            is HomeEvent.OpenGroupScreen -> sendUiEvent(Navigate(route = GroupRoute))

            HomeEvent.OpenFriendsEvent -> {
                sendUiEvent(
                    Navigate(
                        route = FriendsEventRoute
                    )
                )
            }

            is HomeEvent.OpenDetailActivity -> sendUiEvent(
                Navigate(
                    route = DetailActivityRoute(
                        activity = event.activity
                    )
                )
            )

            is HomeEvent.OpenUserProfile -> sendUiEvent(Navigate(route = FriendsProfileRoute(userId = event.userId)))

            is HomeEvent.Follow -> {
                if (!state.value.listTempFollowedUser.contains(event.userId)) {
                    _state.value.listTempFollowedUser.add(event.userId)
                    _state.value.listTempUnfollowedUser.remove(event.userId)

                    _state.value.listActivity.replaceAll { activity ->
                        val user = activity.user
                        if (user?.userId == event.userId) {
                            activity.copy(user = user.copy(isFollowed = true))
                        } else activity
                    }
                }
            }

            is HomeEvent.Unfollow -> {
                if (state.value.listTempFollowedUser.contains(event.userId)) {
                    _state.value.listTempFollowedUser.remove(event.userId)
                } else {
                    _state.value.listTempUnfollowedUser.add(event.userId)
                }

                _state.value.listActivity.replaceAll { activity ->
                    val user = activity.user
                    if (user?.userId == event.userId) {
                        activity.copy(user = user.copy(isFollowed = false))
                    } else activity
                }
            }

            is HomeEvent.JoinEvent -> {
                eventServiceMiddleware.joinEvent(eventId = event.eventId)
                    .enqueue(object : Callback<JoinEventResponse> {
                    override fun onResponse(
                        call: Call<JoinEventResponse>,
                        response: Response<JoinEventResponse>
                    ) {
                        Log.d(TAG, "onResponse: $response")
                        response.body()?.data?.let { eventData ->
                            _state.value.friendsListEvent.replaceAll { oldEvent ->
                                if (oldEvent.id == event.eventId)
                                    eventData.copy(isMember = eventData.isMember)
                                else
                                    oldEvent
                            }

                            _state.value.listRecentlyJoinedEvent.add(eventData.copy(isMember = true))
                        }
                    }

                    override fun onFailure(call: Call<JoinEventResponse>, t: Throwable) {
                        Log.e(TAG, "onFailure: ${t.message}")
                    }
                })
            }

            is HomeEvent.LeaveEvent -> {
                eventServiceMiddleware.leaveEvent(eventId = event.eventId)
                    .enqueue(object : Callback<JoinEventResponse> {
                        override fun onResponse(
                            call: Call<JoinEventResponse>,
                            response: Response<JoinEventResponse>
                        ) {
                            Log.d(TAG, "onResponse: $response")
                            response.body()?.data?.let { eventData ->
                                _state.value.friendsListEvent.replaceAll { oldEvent ->
                                    if (oldEvent.id == event.eventId)
                                        eventData.copy(isMember = eventData.isMember)
                                    else
                                        oldEvent
                                }

                                _state.value.listRecentlyJoinedEvent.removeAll { it.id == event.eventId }
                            }
                        }

                        override fun onFailure(call: Call<JoinEventResponse>, t: Throwable) {
                            Log.e(TAG, "onFailure: ${t.message}")
                        }
                    })
            }

            is HomeEvent.OpenEventDetail -> {
                sendUiEvent(
                    Navigate(
                        route = DetailEventRoute(
                            event = event.event
                        )
                    )
                )
            }

            HomeEvent.ClearRecentlyJoined -> {
                state.value.listRecentlyJoinedEvent.clear()
            }
        }
    }

    fun syncFollowDataWithDB() {
        state.value.listTempFollowedUser.forEach { userId ->
            profileServiceMiddleware.follow(userId).enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        _state.value.listTempFollowedUser.remove(userId)
                    } else {
                        Log.e(
                            "HomeVM",
                            "Failed to follow user $userId: ${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Log.e("HomeVM", "Follow API call failed for user $userId: ${t.message}")
                }
            })
        }

        state.value.listTempUnfollowedUser.forEach { userId ->
            profileServiceMiddleware.unfollow(userId).enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        _state.value.listTempUnfollowedUser.remove(userId)
                    } else {
                        Log.e(
                            "HomeVM",
                            "Failed to unfollow user $userId: ${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Log.e("HomeVM", "Unfollow API call failed for user $userId: ${t.message}")
                }
            })
        }
    }

    fun getHomeData() {
        fetchCurrentLocation()
        fetchCurrentTime()

        _state.value = _state.value.copy(isTripSummaryLoading = true)
        _state.value = _state.value.copy(isWeatherLoading = true)
        _state.value = _state.value.copy(isFriendsEventLoading = true)
        _state.value = _state.value.copy(isActivityLoading = true)

        val tripSummaryCall: Call<TripSummaryResponse> =
            tripServiceMiddleware.getTripSummary("month")

        tripSummaryCall.enqueue(object : Callback<TripSummaryResponse> {
            override fun onResponse(
                call: Call<TripSummaryResponse>,
                response: Response<TripSummaryResponse>
            ) {
                Log.d(TAG, "Trip Summary API Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "Trip Summary API Response Body: $responseBody")

                    responseBody?.data?.let { data ->
                        _state.value = _state.value.copy(
                            tripSummary = data,
                            isTripSummaryLoading = false
                        )

                        Log.d(TAG, "Updated stateFlow with trip summary: ${_state.value.tripSummary}")
                    } ?: {
                        Log.e(TAG, "Trip Summary API Response Body is NULL")
                        _state.value = _state.value.copy(isTripSummaryLoading = false)
                    }

                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Trip Summary API Error: $errorBody")
                    _state.value = _state.value.copy(isTripSummaryLoading = false)
                }
            }

            override fun onFailure(call: Call<TripSummaryResponse>, t: Throwable) {
                Log.e(TAG, "Trip Summary API Failure: ${t.message}")
                _state.value = _state.value.copy(isTripSummaryLoading = false)
            }
        })

        val today = TimeUtil.getFormattedDate(Date(), TimeUtil.FormatSimpleDate)

        val friendsEventCall: Call<EventListResponseMiddleware> =
            eventServiceMiddleware.getListEvent(friend = 1, startFrom = today, limit = state.value.limit, participating = 0)

        friendsEventCall.enqueue(object : Callback<EventListResponseMiddleware> {
            override fun onResponse(
                call: Call<EventListResponseMiddleware>,
                response: Response<EventListResponseMiddleware>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { data ->
                        _state.value.friendsListEvent.apply {
                            clear()
                            addAll(data)
                        }
                    }
                    _state.value = state.value.copy(isFriendsEventLoading = false)
                } else {
                    _state.value = state.value.copy(isFriendsEventLoading = false)
                    Log.e(TAG, "Event API Error: ${response.errorBody()?.string()}")
                    val errorText = response.errorBody()?.string().takeIf { !it.isNullOrBlank() } ?: "No error body"
                    Log.e(TAG, "Event API Error: code=${response.code()}, message=${response.message()}, body=$errorText")
                }
            }

            override fun onFailure(call: Call<EventListResponseMiddleware>, t: Throwable) {
                Log.e(TAG, "Event API Failure: ${t.message}")
                _state.value = state.value.copy(isFriendsEventLoading = false)
            }
        })

        viewModelScope.launch {
            val userId = if (FeatureList.FeatureFriendship) null else profileMiddlewareManager.userId()
            val tripCall: Call<ListTripBodyMiddleware> =
                tripServiceMiddleware.getListActivity(
                    period = "month",
                    userId = userId
                )

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

                            _state.value = state.value.copy(isActivityLoading = false)

                            Log.d(TAG, "Updated listActivity: ${_state.value.listActivity}")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Trip API Error: $errorBody")
                        _state.value = state.value.copy(isActivityLoading = false)

                        when (response.code()) {
                            401 -> {
                                viewModelScope.launch {
                                    _state.value = state.value.copy(isActivityLoading = false)
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
                }

                override fun onFailure(call: Call<ListTripBodyMiddleware>, t: Throwable) {
                    Log.e(TAG, "Trip API Failure: ${t.message}")
                }
            })
        }

    }

    private fun fetchWeather(lat: Double, lng: Double) {
        _state.value = _state.value.copy(isWeatherLoading = true)

        val weatherCall: Call<ListWeatherResponse> = weatherServiceMiddleware.getWeather(lat, lng)

        weatherCall.enqueue(object : Callback<ListWeatherResponse> {
            override fun onResponse(
                call: Call<ListWeatherResponse>,
                response: Response<ListWeatherResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { weatherData ->
                        if (weatherData.isNotEmpty()) {
                            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                            val startIndex = weatherData.indexOfFirst { weather ->
                                weather.time.substring(11, 13).toInt() == currentHour
                            }.takeIf { it >= 0 } ?: weatherData.size

                            val filteredWeather = weatherData.drop(startIndex).take(5)

                            _state.value = _state.value.copy(
                                listWeather = mutableStateListOf(*filteredWeather.toTypedArray()),
                                isWeatherLoading = false
                            )
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Weather API Error: $errorBody")
                    _state.value = _state.value.copy(isWeatherLoading = false)
                }
            }

            override fun onFailure(call: Call<ListWeatherResponse>, t: Throwable) {
                Log.e(TAG, "Weather API Failure: ${t.message}")
                _state.value = _state.value.copy(isWeatherLoading = false)
            }
        })
    }

    private fun fetchCurrentTime() {
        viewModelScope.launch {
            while (true) {
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                _state.value = state.value.copy(time = currentTime)
                delay(1000)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        viewModelScope.launch {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers: List<String> = locationManager.getProviders(true)
            var bestLocation: Location? = null

            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                    bestLocation = location
                }
            }

            bestLocation?.let { loc ->
                val lat = loc.latitude
                val lng = loc.longitude

                Log.d(TAG, "Fetched Location: Lat = $lat, Lng = $lng")

                _state.value = _state.value.copy(latitude = lat, longitude = lng)

                fetchCurrentCity(lat, lng)
                fetchWeather(lat, lng)
            } ?: Log.e(TAG, "Failed to get last known location")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun fetchCurrentCity(lat: Double, lng: Double) {
        _state.value = state.value.copy(isLocationLoading = true)

        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                val cityName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (cont.isActive) {
                                    cont.resume(
                                        addresses.firstOrNull()?.locality
                                            ?: addresses.firstOrNull()?.subAdminArea
                                            ?: "Unknown City",
                                        null
                                    )
                                    _state.value = state.value.copy(isLocationLoading = false)

                                }
                            }

                            override fun onError(errorMessage: String?) {
                                if (cont.isActive) {
                                    cont.resume("Unknown City", null)
                                    _state.value = state.value.copy(isLocationLoading = false)
                                }
                            }
                        })
                    }
                } else {
                    withContext(Dispatchers.IO) {
                        geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()?.locality
                            ?: "Unknown City"
                    }
                }

                Log.d(TAG, "Fetched City Name: $cityName")
                _state.value = state.value.copy(city = cityName)
                _state.value = state.value.copy(isLocationLoading = false)

            } catch (e: Exception) {
                Log.e(TAG, "Geocoder exception: ${e.message}")
                _state.value = state.value.copy(isLocationLoading = false)
            }
        }
    }

    private fun joinEvent(eventId: Int) {
        val currentEvent = state.value.friendsListEvent.find { it.id == eventId } ?: return
        val isCurrentlyMember = currentEvent.isMember

        if (isCurrentlyMember) return

        val call: Call<JoinEventResponse> = eventServiceMiddleware.joinEvent(eventId)

        call.enqueue(object : Callback<JoinEventResponse> {
            override fun onResponse(
                call: Call<JoinEventResponse>,
                response: Response<JoinEventResponse>
            ) {
                Log.d(TAG, "onResponse: $response")

                if (response.isSuccessful) {
                    _state.value.friendsListEvent.replaceAll { event ->
                        if (event.id == eventId) event.copy(isMember = !isCurrentlyMember) else event
                    }

                    _state.value.listRecentlyJoinedEvent.add(currentEvent.copy(isMember = true))
                }
            }

            override fun onFailure(call: Call<JoinEventResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun leaveEvent(eventId: Int) {
        val currentEvent = state.value.friendsListEvent.find { it.id == eventId } ?: return
        val isCurrentlyMember = currentEvent.isMember

        if (!isCurrentlyMember) return

        val call: Call<JoinEventResponse> = eventServiceMiddleware.leaveEvent(eventId)

        call.enqueue(object : Callback<JoinEventResponse> {
            override fun onResponse(
                call: Call<JoinEventResponse>,
                response: Response<JoinEventResponse>
            ) {
                Log.d(TAG, "onResponse: $response")

                if (response.isSuccessful) {
                    _state.value.friendsListEvent.replaceAll { event ->
                        if (event.id == eventId) event.copy(isMember = !isCurrentlyMember) else event
                    }

                    _state.value.listRecentlyJoinedEvent.removeAll { it.id == eventId }
                }
            }

            override fun onFailure(call: Call<JoinEventResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}
