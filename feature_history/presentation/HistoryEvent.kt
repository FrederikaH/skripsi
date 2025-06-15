package com.polygonbikes.ebike.v3.feature_history.presentation

import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware

sealed class HistoryEvent {
    data class OpenHistoryDetail(
        val trip: TripBodyMiddleware
    ) : HistoryEvent()
    data class SetValuePeriod(val value: String) : HistoryEvent()
    object OpenStartTrip : HistoryEvent()
}