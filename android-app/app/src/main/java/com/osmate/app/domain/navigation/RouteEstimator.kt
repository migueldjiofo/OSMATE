package com.osmate.app.domain.navigation

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

object RouteEstimator {
    fun estimateDirectDistance(
        startLatitude: Double,
        startLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
        mode: TravelMode,
    ): RouteEstimate {
        val distanceMeters = calculateDistanceMeters(
            startLatitude = startLatitude,
            startLongitude = startLongitude,
            destinationLatitude = destinationLatitude,
            destinationLongitude = destinationLongitude,
        ).roundToInt()

        val distanceKilometers = distanceMeters / 1000.0
        val durationMinutes = max(
            1,
            ((distanceKilometers / mode.speedKmh) * 60.0).roundToInt(),
        )

        return RouteEstimate(
            distanceMeters = distanceMeters,
            durationMinutes = durationMinutes,
            mode = mode,
        )
    }

    fun formatDistance(distanceMeters: Int): String {
        return if (distanceMeters < 1000) {
            "$distanceMeters m"
        } else {
            val kilometers = distanceMeters / 1000.0
            "%.1f km".format(kilometers)
        }
    }

    fun formatDuration(durationMinutes: Int): String {
        return if (durationMinutes < 60) {
            "$durationMinutes min"
        } else {
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            "${hours} h ${minutes} min"
        }
    }

    private fun calculateDistanceMeters(
        startLatitude: Double,
        startLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
    ): Double {
        val startLatRad = Math.toRadians(startLatitude)
        val destinationLatRad = Math.toRadians(destinationLatitude)
        val deltaLatRad = Math.toRadians(destinationLatitude - startLatitude)
        val deltaLonRad = Math.toRadians(destinationLongitude - startLongitude)

        val a = sin(deltaLatRad / 2) * sin(deltaLatRad / 2) +
            cos(startLatRad) * cos(destinationLatRad) *
            sin(deltaLonRad / 2) * sin(deltaLonRad / 2)

        val c = 2 * atan2(
            sqrt(a),
            sqrt(1 - a),
        )

        return EARTH_RADIUS_METERS * c
    }
}