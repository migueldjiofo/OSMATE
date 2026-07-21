package com.osmate.app.data.model

data class RoutingResult(
    val mode: String,
    val provider: String,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val distanceText: String,
    val durationText: String,
    val geoJson: String,
    val warnings: List<String>,
)