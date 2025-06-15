package com.polygonbikes.ebike.v3.feature_profile.presentation.setting

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.MutableState
import com.polygonbikes.ebike.R
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.navOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.analytics.FirebaseAnalytics
import com.polygonbikes.ebike.core.FeatureList
import com.polygonbikes.ebike.core.di.QualifierModule
import com.polygonbikes.ebike.core.enums.BafangServiceStatus
import com.polygonbikes.ebike.core.route.DetailStravaRoute
import com.polygonbikes.ebike.core.route.LoginRoute
import com.polygonbikes.ebike.core.route.MainRoute
import com.polygonbikes.ebike.core.route.MyAccountV3Route
import com.polygonbikes.ebike.core.route.StravaRoute
import com.polygonbikes.ebike.core.service.EbikeService
import com.polygonbikes.ebike.core.util.AlertDialogData
import com.polygonbikes.ebike.core.util.LocaleHelper
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.RecreateActivity
import com.polygonbikes.ebike.core.util.groupmanager.GroupManager
import com.polygonbikes.ebike.core.util.stravamanager.StravaManager
import com.polygonbikes.ebike.core.util.tokenmanager.TokenManager
import com.polygonbikes.ebike.feature_login.data.login.entities.response.LogoutResponse
import com.polygonbikes.ebike.feature_login.data.remote.AuthService
import com.polygonbikes.ebike.feature_login.util.getGoogleSignInClient
import com.polygonbikes.ebike.feature_me.util.profilemanager.ProfileManager
import com.polygonbikes.ebike.feature_strava.data.remote.StravaAuthService
import com.polygonbikes.ebike.feature_strava.domain.entities.body.StravaDeauthorizationBody
import com.polygonbikes.ebike.feature_strava.domain.entities.response.StravaDeauthorizationResponse
import com.polygonbikes.ebike.v3.feature_profile.domain.state.SettingState
import com.polygonbikes.ebike.v3.feature_profile.util.ProfileMiddlewareManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    @QualifierModule.RetrofitAuth private val authService: AuthService,
    private val stravaManager: StravaManager,
    private val stravaAuthService: StravaAuthService,
    private val tokenManager: TokenManager,
    private val groupManager: GroupManager,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val profileMiddlewareManager: ProfileMiddlewareManager
) : ViewModel() {

    private val TAG = "SettingVM"
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var _state: MutableState<SettingState> = mutableStateOf(SettingState())
    val state: State<SettingState> get() = _state

    private var ebikeService: EbikeService? = null

    init {
        onEvent(SettingEvent.OnInitScreen)
    }

    fun onEvent(event: SettingEvent) {
        when (event) {
            is SettingEvent.OnInitScreen -> {
                _state.value = state.value.copy(
                    isLoggedInStrava = stravaManager.isLoggedIn(),
                    userStravaName = if (stravaManager.isLoggedIn()) stravaManager.getName() else null
                )
            }

            is SettingEvent.OpenMyAccountScreen -> {
                sendUiEvent(UiEvent.Navigate(route = MyAccountV3Route))
            }

            is SettingEvent.SetLanguage -> {
                val localeHelper = LocaleHelper(context)
                localeHelper.setLocale(event.languageKey)
                sendUiEvent(UiEvent.RecreateActivity)
            }

            is SettingEvent.OpenDetailStrava -> {
                sendUiEvent(UiEvent.Navigate(route = DetailStravaRoute))
            }

            is SettingEvent.OpenStrava -> {
                sendUiEvent(UiEvent.Navigate(route = StravaRoute(code = null, scope = null, error = null)))
            }

            is SettingEvent.ShowDialogDisconnectStrava -> {
                sendUiEvent(
                    UiEvent.AlertDialog(
                        AlertDialogData(
                            id = DISCONNECT_STRAVA,
                            title = context.getString(R.string.me_label_dialog_title_disconnect_strava),
                            description = context.getString(R.string.me_label_dialog_description_disconnect_strava),
                            dismissCaption = context.getString(R.string.me_label_dialog_dismiss_disconnect_strava),
                            confirmCaption = context.getString(R.string.me_label_dialog_confirm_disconnect_strava)
                        )
                    )
                )
            }

            is SettingEvent.DisconnectStrava -> {
                disconnectStrava()
            }

            SettingEvent.Logout ->  {
                _state.value = state.value.copy(isLoading = true)
                if (FeatureList.DemoMode) {
                    viewModelScope.launch {
                        delay(1000)
                        _state.value = state.value.copy(isLoading = false)
                        navigateToLanding()
                    }
                    return
                }

                if (
                    state.value.bafangStatus.value != BafangServiceStatus.DISCONNECTED &&
                    state.value.bafangStatus.value != null
                ) {
                    _state.value = state.value.copy(isProcessingLogout = true)
                    ebikeService?.stopRunning()
                    return
                }

                processAPILogout()
            }
        }
    }

    private fun disconnectStrava() {
        stravaManager.getAccessToken()?.let { token ->
            _state.value = state.value.copy(isLoading = true)

            val body = StravaDeauthorizationBody(accessToken = token)
            val call: Call<StravaDeauthorizationResponse> = stravaAuthService.deauthorization(body)

            call.enqueue(object : Callback<StravaDeauthorizationResponse> {
                override fun onResponse(
                    call: Call<StravaDeauthorizationResponse>,
                    response: Response<StravaDeauthorizationResponse>
                ) {
                    _state.value = state.value.copy(isLoading = false)
                    if (response.isSuccessful) {
                        stravaManager.clearData()
                        _state.value = state.value.copy(userStravaName = null)
                    } else {
                        sendUiEvent(UiEvent.ShowSnackBar(message = "Failed to disconnect Strava. Try again later"))
                    }
                }

                override fun onFailure(call: Call<StravaDeauthorizationResponse>, t: Throwable) {
                    _state.value = state.value.copy(isLoading = false)
                    sendUiEvent(UiEvent.ShowSnackBar(message = "Failed to disconnect Strava. Try again later"))
                }
            })
        }
    }

    private fun processAPILogout() {
        _state.value = state.value.copy(isProcessingLogout = false)
        val call: Call<LogoutResponse> = authService.logout()
        call.enqueue(object : Callback<LogoutResponse> {
            override fun onResponse(
                call: Call<LogoutResponse>,
                response: Response<LogoutResponse>
            ) {
                Log.d(TAG, "onResponse: $response")
                if (response.isSuccessful) {
                    val account = GoogleSignIn.getLastSignedInAccount(context)
                    if (account != null) {
                        Log.d(TAG, "Detected login using google")
                        val mGoogleSignin = getGoogleSignInClient(context)
                        mGoogleSignin.signOut()
                            .addOnCompleteListener { processLogout() }
                    } else {
                        processLogout()
                    }
                } else {
                    sendUiEvent(UiEvent.ShowSnackBar(message = context.getString(R.string.me_label_failed_logout)))
                }
                _state.value = state.value.copy(isLoading = false)
            }

            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
                sendUiEvent(UiEvent.ShowSnackBar(message = context.getString(R.string.me_label_failed_logout)))
                _state.value = state.value.copy(isLoading = false)
            }
        })
    }
    
    private fun processLogout() {
        viewModelScope.launch {
            tokenManager.deleteToken()
            stravaManager.clearData()
            profileMiddlewareManager.clearData()
            groupManager.setHaveGroup(false)
            firebaseAnalytics.setUserId(null)

            navigateToLanding()
        }
    }

    private fun navigateToLanding() {
        sendUiEvent(
            UiEvent.Navigate(
                route = LoginRoute(forceLogout = false, reLogin = true),
                navOptions = navOptions {
                    popUpTo<MainRoute> {
                        inclusive = true
                    }
                })
        )
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch { _uiEvent.send(event) }
    }

    companion object {
        const val DISCONNECT_STRAVA = "disconnect_strava"
    }
}