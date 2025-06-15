package com.polygonbikes.ebike.v3.feature_profile.presentation.my_account.changecyclingstyle

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
import com.polygonbikes.ebike.v3.feature_profile.domain.state.ChangeCyclingStyleState
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
class ChangeCyclingStyleViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val profileMiddlewareService: ProfileMiddlewareService,
    private val profileMiddlewareManager: ProfileMiddlewareManager,
) : ViewModel() {

    private val TAG = "ChangeCyclingStyleVM"

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private var _state: MutableState<ChangeCyclingStyleState> = mutableStateOf(ChangeCyclingStyleState())
    val state: State<ChangeCyclingStyleState> get() = _state

    fun onEvent(event: ChangeCyclingStyleEvent) {
        when (event) {
            is ChangeCyclingStyleEvent.InputChangeCyclingStyle -> {
                _state.value = state.value.copy(newCyclingStyle = event.value)
            }

            is ChangeCyclingStyleEvent.ChangeCyclingStyle -> {
                _state.value = state.value.copy(isLoading = true)

                val body = ProfileBodyMiddleware(cyclingStyle = state.value.newCyclingStyle)

                val call: Call<ProfileMiddlewareResponse> = profileMiddlewareService.editProfile(body)

                call.enqueue(object : Callback<ProfileMiddlewareResponse> {
                    override fun onResponse(
                        call: Call<ProfileMiddlewareResponse>,
                        response: Response<ProfileMiddlewareResponse>
                    ) {
                        _state.value = state.value.copy(isLoading = false)
                        if (response.isSuccessful) {
                            sendUiEvent(UiEvent.ShowSnackBar(message = context.getString(R.string.myaccount_changecyclingstyle_label_success_change_cyclingstyle)))
                            response.body()?.data?.let { profileData ->
                                viewModelScope.launch {
                                    profileMiddlewareManager.updateData(cyclingStyle = profileData.cyclingStyle ?: listOf())
                                }
                            }
                            sendUiEvent(UiEvent.PopBackStack)
                        } else {
                            sendUiEvent(UiEvent.ShowSnackBar(message = context.getString(R.string.myaccount_changecyclingstyle_label_failed_change_cyclingstyle)))
                        }
                    }

                    override fun onFailure(call: Call<ProfileMiddlewareResponse>, t: Throwable) {
                        _state.value = state.value.copy(isLoading = false)
                        sendUiEvent(UiEvent.ShowSnackBar(message = context.getString(R.string.myaccount_changecyclingstyle_label_failed_change_cyclingstyle)))
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