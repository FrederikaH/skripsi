package com.polygonbikes.ebike.v3.feature_profile.presentation.my_account.changenickname

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
import com.polygonbikes.ebike.v3.feature_profile.domain.state.ChangeNicknameState
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
class ChangeNicknameViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val profileMiddlewareService: ProfileMiddlewareService,
    private val profileMiddlewareManager: ProfileMiddlewareManager,
) : ViewModel() {

    private val TAG = "ChangeNicknameVM"

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private var _state: MutableState<ChangeNicknameState> = mutableStateOf(ChangeNicknameState())
    val state: State<ChangeNicknameState> get() = _state

    fun onEvent(event: ChangeNicknameEvent) {
        when (event) {
            is ChangeNicknameEvent.InputChangeNickname -> {
                _state.value = state.value.copy(newNickname = event.value)
            }

            is ChangeNicknameEvent.ChangeNickname -> {
                _state.value = state.value.copy(isLoading = true)

                val body = ProfileBodyMiddleware(username = state.value.newNickname)

                val call: Call<ProfileMiddlewareResponse> = profileMiddlewareService.editProfile(body)

                call.enqueue(object : Callback<ProfileMiddlewareResponse> {
                    override fun onResponse(
                        call: Call<ProfileMiddlewareResponse>,
                        response: Response<ProfileMiddlewareResponse>
                    ) {
                        _state.value = state.value.copy(isLoading = false)
                        if (response.isSuccessful) {
                            sendUiEvent(UiEvent.ShowSnackBar(message = context.getString(R.string.me_changename_label_success_change_name)))
                            response.body()?.data?.let { profileData ->
                                viewModelScope.launch {
                                    profileMiddlewareManager.updateData(username = profileData.username ?: "")
                                }
                            }
                            sendUiEvent(UiEvent.PopBackStack)

                        } else {
                            sendUiEvent(UiEvent.ShowSnackBar(message = context.getString(R.string.me_changename_label_failed_change_name)))
                        }
                    }

                    override fun onFailure(call: Call<ProfileMiddlewareResponse>, t: Throwable) {
                        _state.value = state.value.copy(isLoading = false)
                        sendUiEvent(UiEvent.ShowSnackBar(message = context.getString(R.string.me_changename_label_failed_change_name)))
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
