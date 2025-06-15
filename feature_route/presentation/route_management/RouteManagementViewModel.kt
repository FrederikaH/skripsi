package com.polygonbikes.ebike.v3.feature_route.presentation.route_management

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polygonbikes.ebike.core.dao.LogExerciseDao
import com.polygonbikes.ebike.core.database.LogDatabase
import com.polygonbikes.ebike.core.entities.response.LocationResponse
import com.polygonbikes.ebike.core.network.LocationServiceMiddleware
import com.polygonbikes.ebike.core.route.CreateRouteRoute
import com.polygonbikes.ebike.core.route.DetailRouteManagementRoute
import com.polygonbikes.ebike.core.route.RouteRoute
import com.polygonbikes.ebike.core.route.RouteStatisticRoute
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.Navigate
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.ListRouteResponseMiddleware
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.data.remote.RouteServiceMiddleware
import com.polygonbikes.ebike.v3.feature_route.domain.model.RouteFilterData
import com.polygonbikes.ebike.v3.feature_route.domain.state.RouteManagementState
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
class RouteManagementViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val routeServiceMiddleware: RouteServiceMiddleware,
    val locationServiceMiddleware: LocationServiceMiddleware
) : ViewModel() {
    private val TAG = "RouteManagementVM"
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var _state: MutableState<RouteManagementState> = mutableStateOf(RouteManagementState())
    val state: State<RouteManagementState> get() = _state

    private val roomDB: LogDatabase = LogDatabase.getDatabase(context)
    private val logExercise: LogExerciseDao = roomDB.logExerciseDao()

    init {
        getRouteData()
        fetchCityFilter()
    }

    fun onEvent(event: RouteManagementEvent) {
        when (event) {
            is RouteManagementEvent.DeleteRoute -> {
                _state.value = state.value.copy(isLoading = true)
                routeServiceMiddleware.deleteRoute(routeId = event.routeId)
                    .enqueue(object : Callback<RouteData> {
                        override fun onResponse(call: Call<RouteData>, response: Response<RouteData>) {
                            viewModelScope.launch {
                                if (response.isSuccessful) {
                                    getRouteData()
                                    Toast.makeText(context, "Success to delete route", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to delete route", Toast.LENGTH_SHORT).show()
                                }
                                _state.value = state.value.copy(isLoading = false)
                            }
                        }

                        override fun onFailure(call: Call<RouteData>, t: Throwable) {
                            _state.value = state.value.copy(isLoading = false)
                            Toast.makeText(context, "Failed to delete route", Toast.LENGTH_SHORT).show()
                        }
                    })
            }

            is RouteManagementEvent.RestoreRoute -> {
                _state.value = state.value.copy(isLoading = true)
                routeServiceMiddleware.restoreRoute(routeId = event.routeId)
                    .enqueue(object : Callback<RouteData> {
                        override fun onResponse(call: Call<RouteData>, response: Response<RouteData>) {
                            viewModelScope.launch {
                                if (response.isSuccessful) {
                                    getRouteData()
                                    Toast.makeText(context, "Success to restore route", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to restore route", Toast.LENGTH_SHORT).show()
                                }
                                _state.value = state.value.copy(isLoading = false)
                            }
                        }

                        override fun onFailure(call: Call<RouteData>, t: Throwable) {
                            _state.value = state.value.copy(isLoading = false)
                            Toast.makeText(context, "Failed to restore route", Toast.LENGTH_SHORT).show()
                        }
                    })
            }

            is RouteManagementEvent.OpenDetailRoute -> {
                sendUiEvent(
                    Navigate(
                        route = DetailRouteManagementRoute(
                            route = event.route
                        )
                    )
                )
            }

            RouteManagementEvent.OpenCreateRoute -> {
                sendUiEvent(
                    Navigate(
                        route = CreateRouteRoute
                    )
                )
            }

            RouteManagementEvent.OpenRouteStatistic -> {
                sendUiEvent(
                    Navigate(
                        route = RouteStatisticRoute
                    )
                )
            }

            RouteManagementEvent.ShowRouteFilter -> {
                _state.value = state.value.copy(showRouteFilter = !state.value.showRouteFilter)
            }

            is RouteManagementEvent.UpdateRouteFilter -> {
                val filterData = event.routeFilterData

                val isDistanceValid = filterData.distance?.let {
                    val from = it.from ?: 0.0
                    val to = it.to ?: 0.0
                    from <= to
                } ?: true

                val isElevationValid = filterData.elevation?.let {
                    val from = it.from ?: 0
                    val to = it.to ?: 0
                    from <= to
                } ?: true


                if (isDistanceValid && isElevationValid) {
                    _state.value = state.value.copy(
                        routeFilterData = filterData,
                        showRouteFilter = false,
                        isFiltering = filterData.isFiltering()
                    )

                    fetchRoutes(filterData)
                } else {
                    Toast.makeText(context, "From must be less than To", Toast.LENGTH_SHORT).show()
                }
            }

            RouteManagementEvent.InitDataChanges -> {
                getRouteData()
                fetchCityFilter()
            }
        }
    }

    private fun getRouteData() {
        _state.value = state.value.copy(isActiveRouteLoading = true)
        _state.value = state.value.copy(isDeletedRouteLoading = true)

        val activeRouteCall: Call<ListRouteResponseMiddleware> =
            routeServiceMiddleware.getListRoute(
                official = 1,
                deleted = false,
                limit = 100
            )
        
        val deletedRouteCall: Call<ListRouteResponseMiddleware> =
            routeServiceMiddleware.getListRoute(
                official = 1,
                deleted = true,
                limit = 100
            )

        activeRouteCall.enqueue(object : Callback<ListRouteResponseMiddleware> {
            override fun onResponse(
                call: Call<ListRouteResponseMiddleware>,
                response: Response<ListRouteResponseMiddleware>
            ) {
                Log.d(TAG, "API Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "API Response Body: $responseBody")

                    responseBody?.data?.let { data ->
                        Log.d(TAG, "Received event data: $data")
                        _state.value.listActiveRoute.clear()
                        _state.value.listActiveRoute.addAll(data)
                        Log.d(TAG, "Updated listRoute: ${_state.value.listActiveRoute}")
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.errorBody()?.string()}")
                }
                _state.value = state.value.copy(isActiveRouteLoading = false)

            }

            override fun onFailure(call: Call<ListRouteResponseMiddleware>, t: Throwable) {
                Log.e(TAG, "API Failure: ${t.message}")
                _state.value = state.value.copy(isActiveRouteLoading = false)
            }
        })

        deletedRouteCall.enqueue(object : Callback<ListRouteResponseMiddleware> {
            override fun onResponse(
                call: Call<ListRouteResponseMiddleware>,
                response: Response<ListRouteResponseMiddleware>
            ) {
                Log.d(TAG, "API Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "API Response Body: $responseBody")

                    responseBody?.data?.let { data ->
                        Log.d(TAG, "Received event data: $data")
                        _state.value.listDeletedRoute.clear()
                        _state.value.listDeletedRoute.addAll(data)
                        Log.d(TAG, "Updated list deleted route: ${_state.value.listDeletedRoute}")
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.errorBody()?.string()}")
                }
                _state.value = state.value.copy(isDeletedRouteLoading = false)

            }

            override fun onFailure(call: Call<ListRouteResponseMiddleware>, t: Throwable) {
                Log.e(TAG, "API Failure: ${t.message}")
                _state.value = state.value.copy(isDeletedRouteLoading = false)
            }
        })
    }

    private fun fetchRoutes(filter: RouteFilterData) {
        _state.value = state.value.copy(isActiveRouteLoading = true)
        _state.value = state.value.copy(isDeletedRouteLoading = true)
        _state.value = state.value.copy(isFilteredRouteLoading = true)

        val locationLatitude = filter.location?.latitude?.takeIf { it != 0.0 }
        val locationLongitude = filter.location?.longitude?.takeIf { it != 0.0 }
        val locationRadius = filter.location?.radius?.takeIf { it > 0.0 }

        val distanceFrom = filter.distance?.from?.takeIf { it > 0.0 }
        val distanceTo = filter.distance?.to?.takeIf { it > 0.0 }
        val elevationFrom = filter.elevation?.from?.takeIf { it > 0 }
        val elevationTo = filter.elevation?.to?.takeIf { it > 0 }

        val purpose = filter.purpose?.takeIf { it.isNotEmpty() }
        val roads = filter.roads.takeIf { it.isNotEmpty() }?.joinToString(",")
        val cities = filter.cities
            ?.mapNotNull { it.id }
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(",")

        val isFiltering = _state.value.isFiltering == true

        val activeRouteCall = routeServiceMiddleware.getListRoute(
            locationLatitude = if (isFiltering) locationLatitude else null,
            locationLongitude = if (isFiltering) locationLongitude else null,
            locationRadius = if (isFiltering) locationRadius else null,
            purpose = if (isFiltering) purpose else null,
            roads = if (isFiltering) roads else null,
            distanceFrom = if (isFiltering) distanceFrom else null,
            distanceTo = if (isFiltering) distanceTo else null,
            elevationFrom = if (isFiltering) elevationFrom else null,
            elevationTo = if (isFiltering) elevationTo else null,
            cities = if (isFiltering) cities else null,
            official = 1
        )

        activeRouteCall.enqueue(object : Callback<ListRouteResponseMiddleware> {
            override fun onResponse(
                call: Call<ListRouteResponseMiddleware>,
                response: Response<ListRouteResponseMiddleware>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        _state.value.listActiveRoute.apply {
                            clear()
                            addAll(data.data)
                        }
                        _state.value.listDeletedRoute.apply {
                            clear()
                            addAll(data.data)
                        }
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.errorBody()?.string()}")
                }

                _state.value = state.value.copy(isActiveRouteLoading = false)
                _state.value = state.value.copy(isFilteredRouteLoading = false)
            }

            override fun onFailure(call: Call<ListRouteResponseMiddleware>, t: Throwable) {
                Log.e(TAG, "API Failure: ${t.message}")
                _state.value = state.value.copy(isActiveRouteLoading = false)
                _state.value = state.value.copy(isDeletedRouteLoading = false)
                _state.value = state.value.copy(isFilteredRouteLoading = false)
            }
        })
    }

    private fun fetchCityFilter() {
        val eventCall: Call<LocationResponse> = locationServiceMiddleware.GetCityFilter()

        eventCall.enqueue(object : Callback<LocationResponse> {
            override fun onResponse(
                call: Call<LocationResponse>,
                response: Response<LocationResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { cityList ->
                        Log.d(TAG, "available cities: $cityList")

                        _state.value.listCityFilter.clear()
                        _state.value.listCityFilter.apply {
                            clear()
                            addAll(cityList.map { it })
                        }

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