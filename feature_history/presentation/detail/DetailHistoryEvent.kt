package com.polygonbikes.ebike.v3.feature_history.presentation.detail

import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware

sealed class DetailHistoryEvent {
    object InitScreen : DetailHistoryEvent()
    data class GetData(val trip: TripBodyMiddleware) : DetailHistoryEvent()
    object ShareToStrava : DetailHistoryEvent()
    object OpenConnectStrava : DetailHistoryEvent()
    data class OnInputComment(val comment: String) : DetailHistoryEvent()
    object SendComment : DetailHistoryEvent()
    data class OnSetCommentSending(val isEnable: Boolean) : DetailHistoryEvent()
}