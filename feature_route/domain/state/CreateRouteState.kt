package com.polygonbikes.ebike.v3.feature_route.domain.state

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.polygonbikes.ebike.core.entities.LogTrack
import com.polygonbikes.ebike.core.enums.ProcessGPXStatus
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.feature_trip.domain.entities.TripBody
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

data class CreateRouteState (
    val isLoading: Boolean = false,
    val name: String = "",
    val startLocation: String = "Sidoarjo",
    val startLatitude: Double = 0.0,
    val startLongitude: Double = 0.0,
    val roadType: List<String> = listOf(),
    val distance: Double = 0.0,
    val elevation: Int = 0,
    val purpose: String = "",
    val locationId: Int? = null,
    val startLocationData: LocationData? = null,
    val locationCoordinate: LatLng? = null,
    val polyline: String = "",
    val compressingImage: Boolean = false,

    // multiple image
    val headerImageUris: List<Uri> = emptyList(),
    val headerImageFiles: List<File> = emptyList(),

    val gpxFile: File? = null,
    val tripBody: TripBody? = null,
    val tracks: List<LogTrack> = listOf(),
    val showDialogPickMaps: Boolean = false,
    val processGPXStatus: ProcessGPXStatus? = null,
    var isLocationModalShowing: Boolean = false,

    val routeWaypoint: List<LatLng> = mutableListOf(),
    val bounds: MutableStateFlow<LatLngBounds?> = MutableStateFlow(null),

    var isEmptyRouteName: Boolean = false,
    var isEmptyGpxFile: Boolean = false,
    var isEmptyCity: Boolean = false,
    var isEmptyRoadType: Boolean = false,
    var isEmptyPurpose: Boolean = false,
    var isEmptyImage: Boolean = false,

    val selectedCityId: Int? = null,
    val selectedCityName: String = "",
    val listCity: SnapshotStateList<LocationData> = mutableStateListOf(),
    val isCreateRouteEnabled: Boolean = true
)