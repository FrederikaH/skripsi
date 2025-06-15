package com.polygonbikes.ebike.v3.feature_route.presentation.saved_route

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.ListRouteResponseMiddleware
import com.polygonbikes.ebike.v3.feature_route.data.remote.RouteServiceMiddleware
import com.polygonbikes.ebike.v3.feature_route.domain.state.SavedRouteState
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
class SavedRouteViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val routeServiceMiddleware: RouteServiceMiddleware
): ViewModel() {
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val TAG = "SavedRouteVM"
    private var _state: MutableState<SavedRouteState> = mutableStateOf(SavedRouteState())
    val state: State<SavedRouteState> get() = _state

    init {
        getRouteData()
    }

    private fun getRouteData() {
        _state.value = state.value.copy(isRouteLoading = true)
        val call: Call<ListRouteResponseMiddleware> = routeServiceMiddleware.getListSavedRoute()

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
                        Log.d(TAG, "Received event data: ${data}")
                        _state.value.listRoute.addAll(data)
                        Log.d(TAG, "Updated listRoute: ${_state.value.listRoute}")
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.errorBody()?.string()}")
                }
                _state.value = state.value.copy(isRouteLoading = false)
            }

            override fun onFailure(call: Call<ListRouteResponseMiddleware>, t: Throwable) {
                Log.e(TAG, "API Failure: ${t.message}")
                _state.value = state.value.copy(isRouteLoading = false)
            }
        })
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}