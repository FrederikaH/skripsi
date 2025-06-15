package com.polygonbikes.ebike.v3.feature_route.presentation.route_management.create_route

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.polygonbikes.ebike.core.model.LocationData

sealed class CreateRouteEvent {
    data class SetValueName(val value: String) : CreateRouteEvent()
    data class SetValueRoadType(val value: List<String>) : CreateRouteEvent()
    data class SetValuePurpose(val value: String) : CreateRouteEvent()
    data class OnInputLocationId(val value: Int) : CreateRouteEvent()
    data class OnInputLocationCoordinate(val value: LatLng) : CreateRouteEvent()

    // multiple image
    object PickImages : CreateRouteEvent()
    data class SetImageUris(val data: List<Uri>) : CreateRouteEvent()
    data class AddImageUri(val uri: Uri) : CreateRouteEvent()
    data class RemoveImageUri(val uri: Uri) : CreateRouteEvent()

    object Create : CreateRouteEvent()

    object PickGPX : CreateRouteEvent()
    data class SetGpxUri(val data: Uri) : CreateRouteEvent()

    data class SelectCity(val locationData: LocationData?) : CreateRouteEvent()

    data class SetShowModalLocation(val isShowing: Boolean) : CreateRouteEvent()

    object SetCreateEventFalse : CreateRouteEvent()
}