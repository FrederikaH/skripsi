package com.polygonbikes.ebike.v3.feature_trip.presentation.save_trip

import android.net.Uri
import com.polygonbikes.ebike.v3.feature_trip.domain.state.BikeType

sealed class SaveTripEvent {
    data class Init(
        val tripId: String,
        val movingTime: Long,
        val elapsedTime: Long,
        val bikeName: String?
    ) : SaveTripEvent()
    data class SetValueName(val value: String) : SaveTripEvent()
    data class SetValueBikeName(val value: String) : SaveTripEvent()
    data class SetValueRoadType(val value: List<String>) : SaveTripEvent()

    data class SetValuePurpose(val value: String) : SaveTripEvent()

    // single image
    object PickImage : SaveTripEvent()
    data class SetImageUri(val data: Uri) : SaveTripEvent()
    data class SetIsPrivate(val value: Int) : SaveTripEvent()

    // multiple image
    object PickImages : SaveTripEvent()
    data class SetImageUris(val data: List<Uri>) : SaveTripEvent()
    data class AddImageUri(val uri: Uri) : SaveTripEvent()
    data class RemoveImageUri(val uri: Uri) : SaveTripEvent()

    object Create : SaveTripEvent()
    object OpenHistoryScreen : SaveTripEvent()
    data class SetBikeType(val type: BikeType) : SaveTripEvent()
    object ConfirmationDiscardTrip: SaveTripEvent()
    object DiscardTrip: SaveTripEvent()
}