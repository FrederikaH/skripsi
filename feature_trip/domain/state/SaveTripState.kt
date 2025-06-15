package com.polygonbikes.ebike.v3.feature_trip.domain.state

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.polygonbikes.ebike.core.entities.LogTrack
import com.polygonbikes.ebike.feature_trip.domain.entities.TripBody
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

data class SaveTripState (
    val isLoading: Boolean = false,
    val timestamp: Long = 0,
    val name: String = "",
    val movingTime: Long = 0,
    val elapsedTime: Long = 0,
    val avgSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val bikeName: String = "",
    val eBikeName: String? = null,
    var bikeType: BikeType = BikeType.NONEBIKE,
    val startLocation: String = "Sidoarjo",
    val startLatitude: Double = 0.0,
    val startLongitude: Double = 0.0,
    val roadType: List<String> = listOf(),
    val distance: Double = 0.0,
    val elevation: Int = 0,
    val purpose: String = "",
    val polyline: String = "",
    val compressingImage: Boolean = false,

    // single image
    val headerImageBitmap: Bitmap? = null,
    val headerImageFile: File? = null,

    // multiple image
    val headerImageUris: List<Uri> = emptyList(),
    val headerImageFiles: List<File> = emptyList(),

    val gpxFile: File? = null,
    val tripBody: TripBody? = null,
    val tracks: List<LogTrack> = listOf(),

    val routeWaypoint: List<LatLng> = mutableListOf(),
    val bounds: MutableStateFlow<LatLngBounds?> = MutableStateFlow(null),

    var isEmptyTripName: Boolean = false,
    var isEmptyBikeName: Boolean = false,
    var isEmptyRoadType: Boolean = false,
    var isEmptyPurpose: Boolean = false,
    var isEmptyImage: Boolean = false,
    val isPrivate: Int? = 0
)

enum class BikeType {
    EBIKE,
    NONEBIKE
}