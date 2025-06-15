package com.polygonbikes.ebike.v3.feature_profile.presentation.my_account.changelocation

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.feature_me.data.entities.response.ProfileMiddlewareResponse
import com.polygonbikes.ebike.feature_me.data.remote.ProfileMiddlewareService
import com.polygonbikes.ebike.v3.feature_profile.data.entities.body.ProfileBodyMiddleware
import com.polygonbikes.ebike.v3.feature_profile.domain.state.ChangeLocationState
import com.polygonbikes.ebike.v3.feature_profile.util.ProfileMiddlewareManager
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
class ChangeLocationViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val profileMiddlewareService: ProfileMiddlewareService,
    private val profileMiddlewareManager: ProfileMiddlewareManager,
) : ViewModel() {

    private val TAG = "ChangeLocationVM"

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private var _state: MutableState<ChangeLocationState> = mutableStateOf(ChangeLocationState())
    val state: State<ChangeLocationState> get() = _state

    fun onEvent(event: ChangeLocationEvent) {
        when (event) {
            is ChangeLocationEvent.InputChangeLocation -> {
                _state.value = state.value.copy(newLocation = event.value)
            }

            is ChangeLocationEvent.ChangeLocation -> {
                _state.value = state.value.copy(isLoading = true)

                val body = ProfileBodyMiddleware(location = state.value.newLocation)

                val call: Call<ProfileMiddlewareResponse> = profileMiddlewareService.editProfile(body)

                call.enqueue(object : Callback<ProfileMiddlewareResponse> {
                    override fun onResponse(
                        call: Call<ProfileMiddlewareResponse>,
                        response: Response<ProfileMiddlewareResponse>
                    ) {
                        _state.value = state.value.copy(isLoading = false)
                        if (response.isSuccessful) {
                            sendUiEvent(UiEvent.ShowSnackBar(message = context.getString(R.string.myaccount_changelocation_label_success_change_location)))
                            response.body()?.data?.let { profileData ->
                                viewModelScope.launch {
                                    profileMiddlewareManager.updateData(location = profileData.location ?: "")
                                }
                            }
                            sendUiEvent(UiEvent.PopBackStack)
                        } else {
                            sendUiEvent(UiEvent.ShowSnackBar(message = context.getString(R.string.myaccount_changelocation_label_failed_change_location)))
                        }
                    }

                    override fun onFailure(call: Call<ProfileMiddlewareResponse>, t: Throwable) {
                        _state.value = state.value.copy(isLoading = false)
                        sendUiEvent(UiEvent.ShowSnackBar(message = context.getString(R.string.myaccount_changelocation_label_failed_change_location)))
                    }
                })
            }
        }
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}
