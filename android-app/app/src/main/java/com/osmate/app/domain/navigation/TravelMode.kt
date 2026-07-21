package com.osmate.app.domain.navigation

enum class TravelMode(
    val label: String,
    val speedKmh: Double,
    val mapsTravelMode: String,
) {
    Walking(
        label = "Fußweg",
        speedKmh = 5.0,
        mapsTravelMode = "walking",
    ),
    Bicycle(
        label = "Fahrrad",
        speedKmh = 15.0,
        mapsTravelMode = "bicycling",
    ),
    Car(
        label = "Auto",
        speedKmh = 35.0,
        mapsTravelMode = "driving",
    ),
}