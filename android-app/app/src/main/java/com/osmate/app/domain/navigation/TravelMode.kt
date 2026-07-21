package com.osmate.app.domain.navigation

enum class TravelMode(
    val label: String,
    val apiValue: String,
    val speedKmh: Double,
    val mapsTravelMode: String,
) {
    Walking(
        label = "Fußweg",
        apiValue = "walking",
        speedKmh = 5.0,
        mapsTravelMode = "walking",
    ),
    Bicycle(
        label = "Fahrrad",
        apiValue = "bicycle",
        speedKmh = 15.0,
        mapsTravelMode = "bicycling",
    ),
    Car(
        label = "Auto",
        apiValue = "car",
        speedKmh = 35.0,
        mapsTravelMode = "driving",
    ),
}