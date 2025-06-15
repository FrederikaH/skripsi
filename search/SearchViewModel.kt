package com.polygonbikes.ebike.v3.feature_home.presentation.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polygonbikes.ebike.core.network.LocationServiceMiddleware
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.v3.feature_group.data.remote.GroupServiceMiddleware
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val groupServiceMiddleware: GroupServiceMiddleware,
) : ViewModel() {
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val TAG = "SearchVM"

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}