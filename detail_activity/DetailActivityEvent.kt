package com.polygonbikes.ebike.v3.feature_home.presentation.detail_activity

import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware

sealed class DetailActivityEvent {
    data class GetData(val activity: TripBodyMiddleware) : DetailActivityEvent()
    data class OnInputComment(val comment: String) : DetailActivityEvent()
    object SendComment : DetailActivityEvent()
    data class OnSetCommentSending(val isEnable: Boolean) : DetailActivityEvent()
}