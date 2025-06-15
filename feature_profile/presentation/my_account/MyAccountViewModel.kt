package com.polygonbikes.ebike.v3.feature_profile.presentation.my_account

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.navOptions
import com.polygonbikes.ebike.core.dao.LogExerciseDao
import com.polygonbikes.ebike.core.database.LogDatabase
import com.polygonbikes.ebike.core.route.ChangeCyclingStyleRoute
import com.polygonbikes.ebike.core.route.ChangeLocationRoute
import com.polygonbikes.ebike.core.route.ChangeNicknameRoute
import com.polygonbikes.ebike.core.route.LoginRoute
import com.polygonbikes.ebike.core.route.MainRoute
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.*
import com.polygonbikes.ebike.core.util.groupmanager.GroupManager
import com.polygonbikes.ebike.core.util.stravamanager.StravaManager
import com.polygonbikes.ebike.core.util.tokenmanager.TokenManager
import com.polygonbikes.ebike.feature_me.util.profilemanager.ProfileManager
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileResponse
import com.polygonbikes.ebike.v3.feature_profile.data.remote.ProfileServiceMiddleware
import com.polygonbikes.ebike.v3.feature_profile.domain.state.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MyAccountViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val profileManager: ProfileManager,
    private val profileServiceMiddleware: ProfileServiceMiddleware,
    private val tokenManager: TokenManager,
    private val stravaManager: StravaManager,
    private val groupManager: GroupManager
) : ViewModel() {

    private val TAG = "MyAccountVM"
    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private var _stateFlow = MutableStateFlow(ProfileState())
    val stateFlow = _stateFlow.asStateFlow()

    private val roomDB: LogDatabase = LogDatabase.getDatabase(context)
    private val logExercise: LogExerciseDao = roomDB.logExerciseDao()

    init {
        loadProfileData(stateFlow.value.period.toString())
    }

    private fun loadProfileData(period: String) {
        _stateFlow.value = _stateFlow.value.copy(isLoading = true)

        val profileCall: Call<ProfileResponse> = profileServiceMiddleware.getOwnProfile()

        profileCall.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(
                call: Call<ProfileResponse>,
                response: Response<ProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val profileData = response.body()?.data
                    if (profileData != null) {
                        _stateFlow.value = _stateFlow.value.copy(
                            profile = profileData,
                            isLoading = false
                        )
                    } else {
                        _stateFlow.value = _stateFlow.value.copy(
                            isLoading = false,
                            errorMessage = "Profile data is null."
                        )
                    }
                } else {
                    _stateFlow.value = _stateFlow.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load profile. Response not successful."
                    )
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                _stateFlow.value = _stateFlow.value.copy(
                    isLoading = false,
                    errorMessage = "Network error: ${t.message}"
                )
            }
        })
    }

    fun onEvent(event: MyAccountEvent) {
        when (event) {
            MyAccountEvent.OpenChangeCyclingStyle -> {
                 sendUiEvent(Navigate(route = ChangeCyclingStyleRoute))
            }
            MyAccountEvent.OpenChangeLocation -> {
                 sendUiEvent(Navigate(route = ChangeLocationRoute))
            }
            MyAccountEvent.OpenChangeNickname -> {
                sendUiEvent(Navigate(route = ChangeNicknameRoute))
            }
            MyAccountEvent.InitDataChanges -> {
                loadProfileData(stateFlow.value.period.toString())
            }
            MyAccountEvent.DeleteAccount -> {
                _stateFlow.value = stateFlow.value.copy(isLoading = true)

                profileServiceMiddleware
                    .deleteProfile()
                    .enqueue(object : Callback<ProfileResponse> {
                        override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                            viewModelScope.launch {
                                if (response.isSuccessful) {
                                    tokenManager.deleteToken()
                                    stravaManager.clearData()
                                    profileManager.clearData()
                                    groupManager.setHaveGroup(false)

                                    _stateFlow.value = stateFlow.value.copy(isLoading = false)

                                    sendUiEvent(
                                        UiEvent.Navigate(
                                            route = LoginRoute(forceLogout = false, reLogin = true),
                                            navOptions = navOptions {
                                                popUpTo<MainRoute> { inclusive = true }
                                            }
                                        )
                                    )
                                } else {
                                    _stateFlow.value = stateFlow.value.copy(isLoading = false)
                                    sendUiEvent(UiEvent.ShowSnackBar("event","Failed to delete account"))
                                }
                            }
                        }

                        override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                            _stateFlow.value = stateFlow.value.copy(isLoading = false)
                            sendUiEvent(UiEvent.ShowSnackBar("event","Failed to delete account"))
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