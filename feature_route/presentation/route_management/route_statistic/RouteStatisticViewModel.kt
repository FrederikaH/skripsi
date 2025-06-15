package com.polygonbikes.ebike.v3.feature_route.presentation.route_management.route_statistic

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polygonbikes.ebike.core.database.LogDatabase
import com.polygonbikes.ebike.core.route.DetailRouteManagementRoute
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.Navigate
import com.polygonbikes.ebike.v3.feature_profile.presentation.ProfileEvent
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RoadTypeCountResponseMiddleware
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.ListRouteResponseMiddleware
import com.polygonbikes.ebike.v3.feature_route.data.remote.RouteServiceMiddleware
import com.polygonbikes.ebike.v3.feature_route.domain.state.RouteStatisticState
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
class RouteStatisticViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val routeServiceMiddleware: RouteServiceMiddleware
) : ViewModel() {
    private val TAG = "RouteStatisticVM"
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var _state: MutableState<RouteStatisticState> = mutableStateOf(RouteStatisticState())
    val state: State<RouteStatisticState> get() = _state

    private val roomDB: LogDatabase = LogDatabase.getDatabase(context)

    init {
        fetchMostSavedRoute()
        fetchRoadTypesCount()
    }

    fun onEvent(event: RouteStatisticEvent) {
        when (event) {
            is RouteStatisticEvent.OpenDetailRoute -> {
                sendUiEvent(
                    Navigate(
                        route = DetailRouteManagementRoute(
                            route = event.route
                        )
                    )
                )
            }

            is RouteStatisticEvent.SetValuePeriod -> {
                _state.value = state.value.copy(period = event.value)
                fetchRoadTypesCount(event.value)
            }
        }
    }

    private fun fetchRoadTypesCount(period: String? = "month") {
        val call: Call<RoadTypeCountResponseMiddleware> = routeServiceMiddleware.getRoadTypesCount(period = period ?: "month")

        call.enqueue(object : Callback<RoadTypeCountResponseMiddleware> {
            override fun onResponse(
                call: Call<RoadTypeCountResponseMiddleware>,
                response: Response<RoadTypeCountResponseMiddleware>
            ) {
                Log.d(TAG, "API Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "API Response Body: $responseBody")

                    responseBody?.data?.let { data ->
                        _state.value = _state.value.copy(
                            roadTypesCount = responseBody.data,
                            isRoadTypeLoading = false
                        )
                        Log.d(TAG, "Updated road types count: ${_state.value.roadTypesCount}")
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.errorBody()?.string()}")
                }
                _state.value = state.value.copy(isRoadTypeLoading = false)
            }

            override fun onFailure(call: Call<RoadTypeCountResponseMiddleware>, t: Throwable) {
                Log.e(TAG, "API Failure: ${t.message}")
                _state.value = state.value.copy(isRoadTypeLoading = false)
            }
        })
    }

    private fun fetchMostSavedRoute() {
        val call: Call<ListRouteResponseMiddleware> = routeServiceMiddleware.getListMostSavedRoute()

        call.enqueue(object : Callback<ListRouteResponseMiddleware> {
            override fun onResponse(
                call: Call<ListRouteResponseMiddleware>,
                response: Response<ListRouteResponseMiddleware>
            ) {
                Log.d(TAG, "API Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "API Response Body: $responseBody")

                    responseBody?.data?.let { data ->
                        _state.value.listMostSavedRoute.addAll(data)
                        Log.d(TAG, "Updated listMostSavedRoute: ${_state.value.listMostSavedRoute}")
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.errorBody()?.string()}")
                }
                _state.value = state.value.copy(isMostSavedRouteLoading = false)
            }

            override fun onFailure(call: Call<ListRouteResponseMiddleware>, t: Throwable) {
                Log.e(TAG, "API Failure: ${t.message}")
                _state.value = state.value.copy(isMostSavedRouteLoading = false)
            }
        })
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}