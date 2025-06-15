package com.polygonbikes.ebike.v3.feature_route.presentation.detail

import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData

sealed class DetailRouteEvent {
    data class GetData(val route: RouteData) : DetailRouteEvent()
    object DownloadGPX : DetailRouteEvent()
    object SaveRouteDialog: DetailRouteEvent()
    data class SaveRoute (val name: String) : DetailRouteEvent()
    data class OpenCreateEventScreen (val route: RouteData) : DetailRouteEvent()
    data class OnInputComment(val comment: String) : DetailRouteEvent()
    object SendComment : DetailRouteEvent()
    data class OnSetCommentSending(val isEnable: Boolean) : DetailRouteEvent()

}