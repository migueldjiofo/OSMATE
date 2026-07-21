package com.osmate.app.data.repository

import com.osmate.app.data.api.OsmateApiClient
import com.osmate.app.data.model.RoutingResult
import com.osmate.app.data.model.SearchResult

class SearchRepository(
    private val apiClient: OsmateApiClient = OsmateApiClient(),
) {
    suspend fun getBackendHealth(): String {
        return apiClient.getBackendHealth()
    }

    suspend fun search(
        query: String,
        placeName: String?,
        latitude: Double?,
        longitude: Double?,
        radiusMeters: Int,
        limit: Int,
    ): SearchResult {
        return apiClient.search(
            query = query,
            placeName = placeName,
            latitude = latitude,
            longitude = longitude,
            radiusMeters = radiusMeters,
            limit = limit,
        )
    }

    suspend fun calculateRoute(
        startLatitude: Double,
        startLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
        mode: String,
    ): RoutingResult {
        return apiClient.calculateRoute(
            startLatitude = startLatitude,
            startLongitude = startLongitude,
            destinationLatitude = destinationLatitude,
            destinationLongitude = destinationLongitude,
            mode = mode,
        )
    }
}