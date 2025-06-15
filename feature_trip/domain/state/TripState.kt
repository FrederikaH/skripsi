package com.polygonbikes.ebike.v3.feature_trip.domain.state

import java.sql.Timestamp

class TripState (
    val userId: Int = 0,
    val name: String = "",
    val movingTime: Int = 0,
    val elapsedTime: Int = 0,
    val avgSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val bikeId: Int = 0,
    val routeId: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val deletedAt: Timestamp? = null
)