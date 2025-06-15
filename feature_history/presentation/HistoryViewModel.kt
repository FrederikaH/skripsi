package com.polygonbikes.ebike.v3.feature_history.presentation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polygonbikes.ebike.core.dao.LogExerciseDao
import com.polygonbikes.ebike.core.database.LogDatabase
import com.polygonbikes.ebike.core.route.DetailHistoryV3Route
import com.polygonbikes.ebike.core.route.TripRoute
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.*
import com.polygonbikes.ebike.v3.feature_history.domain.state.HistoryState
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
class HistoryViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val tripServiceMiddleware: TripServiceMiddleware,
    private val profileMiddlewareManager: ProfileMiddlewareManager
) : ViewModel() {
    private val TAG = "HistoryVM"
    private var _state: MutableState<HistoryState> = mutableStateOf(HistoryState())
    val state: State<HistoryState> get() = _state

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()


    private val roomDB: LogDatabase = LogDatabase.getDatabase(context)
    private val logExercise : LogExerciseDao = roomDB.logExerciseDao()

    init {
        viewModelScope.launch {
            val userId = profileMiddlewareManager.userId()
            Log.d(TAG, "HistoryViewModel is initialized!")

            _state.value = state.value.copy(
                userId = userId
            )
            getTripData("all", isInitialLoad = true)
        }
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.OpenHistoryDetail -> {
                sendUiEvent(
                    Navigate(
                        route = DetailHistoryV3Route(
                            trip = event.trip
                        )
                    )
                )
            }

            is HistoryEvent.SetValuePeriod -> {
                _state.value = state.value.copy(period = event.value)
                getTripData(event.value)
            }

            is HistoryEvent.OpenStartTrip -> {
                sendUiEvent(Navigate(route = TripRoute(code = null)))
            }
        }
    }

    private fun getTripData(period: String, isInitialLoad: Boolean = false) {
        _state.value = state.value.copy(isHistoryLoading = true)

        val userId = state.value.userId

        val call: Call<ListTripBodyMiddleware> = tripServiceMiddleware.getListTrip(period = period, userId = userId?.toInt())

        call.enqueue(object : Callback<ListTripBodyMiddleware> {
            override fun onResponse(call: Call<ListTripBodyMiddleware>, response: Response<ListTripBodyMiddleware>) {
                Log.d(TAG, "API Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "API Response Body: $responseBody")

                    responseBody?.data?.let { data ->
                        Log.d(TAG, "Received trip data: $data")

                        if (isInitialLoad) {
                            val isEmpty = data.isEmpty()
                            Log.d(TAG, "Initial load - isHistoryEmpty: $isEmpty")

                            _state.value = state.value.copy(isHistoryEmpty = data.isEmpty())
                        }

                        _state.value = state.value.copy(
                            listHistory = data.toMutableStateList()
                        )
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.errorBody()?.string()}")
                }
                _state.value = state.value.copy(isHistoryLoading = false)
            }

            override fun onFailure(call: Call<ListTripBodyMiddleware>, t: Throwable) {
                Log.e(TAG, "API Failure: ${t.message}")
                _state.value = state.value.copy(isHistoryLoading = false)
            }
        })
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}