package com.osmate.app.domain.navigation

data class RouteEstimate(
    val distanceMeters: Int,
    val durationMinutes: Int,
    val mode: TravelMode,
)