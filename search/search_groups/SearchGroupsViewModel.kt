package com.polygonbikes.ebike.v3.feature_home.presentation.search.search_groups

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polygonbikes.ebike.core.entities.response.LocationResponse
import com.polygonbikes.ebike.core.network.LocationServiceMiddleware
import com.polygonbikes.ebike.core.route.DetailGroupRouteMiddleware
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.Navigate
import com.polygonbikes.ebike.v3.feature_event.domain.state.CityFilterItem
import com.polygonbikes.ebike.v3.feature_group.data.entities.response.ListGroupResponseMiddleware
import com.polygonbikes.ebike.v3.feature_group.data.remote.GroupServiceMiddleware
import com.polygonbikes.ebike.v3.feature_home.domain.state.SearchGroupState
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
class SearchGroupsViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val groupServiceMiddleware: GroupServiceMiddleware,
    private val locationServiceMiddleware: LocationServiceMiddleware
) : ViewModel() {
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val TAG = "SearchGroupVM"
    private var _state: MutableState<SearchGroupState> = mutableStateOf(SearchGroupState())
    val state: State<SearchGroupState> get() = _state

    private var searchJob: Job? = null

    init {
        fetchGroups()
        fetchCity()
    }

    fun onEvent(event: SearchGroupsEvent) {
        when (event) {
            is SearchGroupsEvent.OpenGroupsDetail -> {
                _state.value = state.value.copy(isLoading = true)
                sendUiEvent(
                    Navigate(
                        route = DetailGroupRouteMiddleware(
                            group = event.group,
                            groupId = event.group.id
                        )
                    )
                )
            }

            is SearchGroupsEvent.SearchKeywordChanged -> {
                _state.value = state.value.copy(searchKeyword = event.keyword)

                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500)
                    fetchGroups()
                }
            }


            SearchGroupsEvent.ShowLocationFilter -> {
                _state.value =
                    state.value.copy(showLocationFilter = !state.value.showLocationFilter)
            }

            is SearchGroupsEvent.UpdateSelectedCities -> {
                _state.value = state.value.copy(selectedCities = event.selectedCities, showLocationFilter = false)
                fetchGroups()
            }

            SearchGroupsEvent.ShowCyclingTypeFilter -> {
                _state.value = state.value.copy(showCyclingTypeFilter = !state.value.showCyclingTypeFilter)
            }

            is SearchGroupsEvent.UpdateCyclingTypeFilter -> {
                _state.value = state.value.copy(
                    selectedCyclingType = event.cyclingType
                )
                fetchGroups()
            }

            SearchGroupsEvent.ShowPurposeFilter -> {
                _state.value = state.value.copy(showPurposeFilter = !state.value.showPurposeFilter)
            }

            is SearchGroupsEvent.UpdatePurposeFilter -> {
                _state.value = state.value.copy(
                    selectedPurpose = event.purpose
                )
                fetchGroups()
            }

            SearchGroupsEvent.InitDataChanges -> {
                fetchGroups()
            }
        }
    }

    private fun fetchGroups() {
        _state.value = state.value.copy(isLoading = true)
        val currentState = state.value
        val name = currentState.searchKeyword.takeIf { it.isNotBlank() }
        val cities = currentState.selectedCities.takeIf { it.isNotEmpty() }?.joinToString(",")
        val types = currentState.selectedCyclingType.takeIf { it.isNotEmpty() }?.joinToString(",")
        val purposes = currentState.selectedPurpose.takeIf { it.isNotEmpty() }?.joinToString(",")


        val notJoinedGroupCall = groupServiceMiddleware.getListGroup(
            name = name,
            cities = cities,
            types = types,
            purposes = purposes,
            participating = 0
        )

        val joinedGroupCall = groupServiceMiddleware.getListGroup(
            name = name,
            cities = cities,
            types = types,
            purposes = purposes,
            participating = 1
        )


        notJoinedGroupCall.enqueue(object : Callback<ListGroupResponseMiddleware> {
            override fun onResponse(
                call: Call<ListGroupResponseMiddleware>,
                response: Response<ListGroupResponseMiddleware>
            ) {
                Log.d(TAG, "API Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "API Response Body: $responseBody")

                    responseBody?.data?.let { data ->
                        Log.d(TAG, "Received event data: ${data}")
                        _state.value.listNotJoinedGroup.clear()
                        _state.value.listNotJoinedGroup.addAll(data)
                        Log.d(TAG, "Updated listRoute: ${_state.value.listNotJoinedGroup}")
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.errorBody()?.string()}")
                }
                _state.value = state.value.copy(isLoading = false)
            }

            override fun onFailure(call: Call<ListGroupResponseMiddleware>, t: Throwable) {
                Log.e(TAG, "API Failure: ${t.message}")
                _state.value = state.value.copy(isLoading = false)
            }
        })

        joinedGroupCall.enqueue(object : Callback<ListGroupResponseMiddleware> {
            override fun onResponse(
                call: Call<ListGroupResponseMiddleware>,
                response: Response<ListGroupResponseMiddleware>
            ) {
                Log.d(TAG, "API Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "API Response Body: $responseBody")

                    responseBody?.data?.let { data ->
                        Log.d(TAG, "Received event data: ${data}")
                        _state.value.listJoinedGroup.clear()
                        _state.value.listJoinedGroup.addAll(data)
                        Log.d(TAG, "Updated listRoute: ${_state.value.listJoinedGroup}")
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.errorBody()?.string()}")
                }
                _state.value = state.value.copy(isLoading = false)
            }

            override fun onFailure(call: Call<ListGroupResponseMiddleware>, t: Throwable) {
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