package com.polygonbikes.ebike.v3.feature_route.domain.model

data class RouteFilterData(
    var location: Location? = null,
    val purpose: String? = null,
    val roads: List<String> = emptyList(),
    val distance: Distance? = null,
    val elevation: Elevation? = null,
    val cities: List<com.polygonbikes.ebike.core.model.LocationData>? = emptyList()
) {
    companion object {
        val emptyState = RouteFilterData()
    }

    fun isFiltering(): Boolean {
        return (location?.radius ?: 0.0) > 0.0 ||
                !purpose.isNullOrBlank() ||
                roads.isNotEmpty() ||
                (distance?.from ?: 0.0) > 0.0 ||
                (distance?.to ?: 0.0) > 0.0 ||
                (elevation?.from ?: 0) > 0 ||
                (elevation?.to ?: 0) > 0 ||
                !cities.isNullOrEmpty()
    }
}

data class Location(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var radius: Double = 0.0,
)

data class Distance(
    var from: Double? = null,
    var to: Double? = null,
)

data class Elevation(
    var from: Int? = null,
    var to: Int? = null,
)