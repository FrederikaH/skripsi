package com.polygonbikes.ebike.v3.feature_home.presentation.search.search_friends

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polygonbikes.ebike.core.entities.response.LocationResponse
import com.polygonbikes.ebike.core.network.LocationServiceMiddleware
import com.polygonbikes.ebike.core.route.FriendsProfileRoute
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.Navigate
import com.polygonbikes.ebike.v3.feature_event.domain.state.CityFilterItem
import com.polygonbikes.ebike.v3.feature_home.domain.state.SearchFriendsState
import com.polygonbikes.ebike.v3.feature_profile.data.entities.body.FindUserBody
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ListUserResponseMiddleware
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileResponse
import com.polygonbikes.ebike.v3.feature_profile.data.remote.ProfileServiceMiddleware
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SearchFriendsViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val profileServiceMiddleware: ProfileServiceMiddleware,
    private val locationServiceMiddleware: LocationServiceMiddleware,
) : ViewModel() {
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val TAG = "SearchGroupVM"
    private var _state: MutableState<SearchFriendsState> = mutableStateOf(SearchFriendsState())
    val state: State<SearchFriendsState> get() = _state

    private var searchJob: Job? = null

    init {
        fetchUserSuggestions()
        fetchCity()
    }

    fun onEvent(event: SearchFriendsEvent) {
        when (event) {
            SearchFriendsEvent.InitDataChanges -> {
                fetchUsers()
            }

            is SearchFriendsEvent.OpenUserProfile -> {
                sendUiEvent(
                    Navigate(
                        route = FriendsProfileRoute(userId = event.userId)
                    )
                )
            }

            is SearchFriendsEvent.SearchKeywordChanged -> {
                _state.value = state.value.copy(searchKeyword = event.keyword)

                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500)
                    fetchUsers()
                }
            }

            SearchFriendsEvent.ShowLocationFilter -> {
                _state.value =
                    state.value.copy(showLocationFilter = !state.value.showLocationFilter)
            }

            is SearchFriendsEvent.UpdateSelectedCities -> {
                _state.value = state.value.copy(selectedCities = event.selectedCities, showLocationFilter = false)
                fetchUsers()
            }

            SearchFriendsEvent.ShowCyclingTypeFilter -> {
                _state.value = state.value.copy(showCyclingTypeFilter = !state.value.showCyclingTypeFilter)
            }

            is SearchFriendsEvent.UpdateCyclingTypeFilter -> {
                _state.value = state.value.copy(
                    selectedCyclingType = event.cyclingType
                )
                fetchUsers()
            }

            is SearchFriendsEvent.Follow -> {
                val user = state.value.listUser.find { it.userId == event.userId } ?: return
                if (user.isFollowed == true || user.userId == null) return

                val userId = user.userId ?: return

                _state.value = state.value.copy(isLoading = true)

                profileServiceMiddleware.follow(userId).enqueue(object : Callback<ProfileResponse> {
                    override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                        _state.value = state.value.copy(isLoading = false)

                        if (response.isSuccessful) {
                            user.isFollowed = true
                        }
                    }

                    override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                        Log.e(TAG, "onFailure: ${t.message}")
                        _state.value = state.value.copy(isLoading = false)
                    }
                })
            }

            is SearchFriendsEvent.Unfollow -> {
                val user = state.value.listUser.find { it.userId == event.userId } ?: return
                if (user.isFollowed == false || user.userId == null) return

                val userId = user.userId ?: return

                _state.value = state.value.copy(isLoading = true)

                profileServiceMiddleware.unfollow(userId).enqueue(object : Callback<ProfileResponse> {
                    override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                        _state.value = state.value.copy(isLoading = false)

                        if (response.isSuccessful) {
                            user.isFollowed = false
                        }
                    }

                    override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                        Log.e(TAG, "onFailure: ${t.message}")
                        _state.value = state.value.copy(isLoading = false)
                    }
                })
            }
        }
    }

    private fun fetchUsers() {
        _state.value = state.value.copy(isLoading = true)

        val currentState = state.value
        val name =
            currentState.searchKeyword.takeIf { it.isNotBlank() }
        val cities = currentState.selectedCities.joinToString(",").takeIf { it.isNotBlank() }
        val types = currentState.selectedCyclingType.joinToString(",").takeIf { it.isNotBlank() }

        val body = FindUserBody(name = name)

        _state.value = state.value.copy(
            isSearchFilterEmpty = name.isNullOrBlank() && cities.isNullOrBlank() && types.isNullOrBlank()
        )

        if (state.value.isSearchFilterEmpty == true) {
            fetchUserSuggestions()
            return
        }

        profileServiceMiddleware.getListUser(body, cities, types)
            .enqueue(object : Callback<ListUserResponseMiddleware> {
                override fun onResponse(
                    call: Call<ListUserResponseMiddleware>,
                    response: Response<ListUserResponseMiddleware>
                ) {
                    _state.value = state.value.copy(isLoading = false)

                    if (response.isSuccessful) {
                        response.body()?.data?.let { data ->
                            _state.value.listUser.apply {
                                clear()
                                addAll(data)
                            }
                        }
                    } else {
                        Log.e(TAG, "API Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ListUserResponseMiddleware>, t: Throwable) {
                    Log.e(TAG, "API Failure: ${t.message}")
                    _state.value = state.value.copy(isLoading = false)
                }
            })
    }

    private fun fetchUserSuggestions() {
        _state.value = state.value.copy(isLoading = true)

        profileServiceMiddleware.getUserSuggestions()
            .enqueue(object : Callback<ListUserResponseMiddleware> {
                override fun onResponse(
                    call: Call<ListUserResponseMiddleware>,
                    response: Response<ListUserResponseMiddleware>
                ) {
                    _state.value = state.value.copy(isLoading = false)

                    if (response.isSuccessful) {
                        response.body()?.data?.let { data ->
                            _state.value.listUser.apply {
                                clear()
                                addAll(data)
                            }
                        }
                    } else {
                        Log.e(TAG, "API Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ListUserResponseMiddleware>, t: Throwable) {
                    Log.e(TAG, "API Failure: ${t.message}")
                    _state.value = state.value.copy(isLoading = false)
                }
            })
    }

    private fun fetchCity() {
        val eventCall: Call<LocationResponse> = locationServiceMiddleware.GetLocation()

        eventCall.enqueue(object : Callback<LocationResponse> {
            override fun onResponse(call: Call<LocationResponse>, response: Response<LocationResponse>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { locationList ->
                        Log.d(TAG, "Available cities: $locationList")

                        val cityFilterList = locationList.mapNotNull { location ->
                            if (location.id != null && location.city != null) {
                                CityFilterItem(id = location.id.toString(), city = location.city)
                            } else null
                        }.distinctBy { it.id }

                        _state.value = state.value.copy(
                            listCityFilter = cityFilterList
                        )

                    }
                } else {
                    Log.e(TAG, "City API Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<LocationResponse>, t: Throwable) {
                Log.e(TAG, "City API Failure: ${t.message}")
            }
        })
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}