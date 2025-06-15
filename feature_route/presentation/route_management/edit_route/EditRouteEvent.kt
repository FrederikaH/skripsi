package com.polygonbikes.ebike.v3.feature_route.presentation.route_management.edit_route

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData

sealed class EditRouteEvent {
    data class SetValueName(val value: String) : EditRouteEvent()
    data class SetValueRoadType(val value: List<String>) : EditRouteEvent()
    data class SetValuePurpose(val value: String) : EditRouteEvent()
    data class OnInputLocationId(val value: Int) : EditRouteEvent()
    data class OnInputLocationCoordinate(val value: LatLng) : EditRouteEvent()

    // multiple image
    object PickImages : EditRouteEvent()
    data class SetImageUris(val data: List<Uri>) : EditRouteEvent()
    data class AddImageUri(val uri: Uri) : EditRouteEvent()
    data class RemoveImageUri(val uri: Uri) : EditRouteEvent()

    object Edit : EditRouteEvent()

    object PickGPX : EditRouteEvent()
    data class SetGpxUri(val data: Uri) : EditRouteEvent()

    data class SelectCity(val locationData: LocationData?) : EditRouteEvent()

    data class SetShowModalLocation(val isShowing: Boolean) : EditRouteEvent()

    object SetEditRouteFalse : EditRouteEvent()

    data class GetRouteData(val route: RouteData) : EditRouteEvent()
}