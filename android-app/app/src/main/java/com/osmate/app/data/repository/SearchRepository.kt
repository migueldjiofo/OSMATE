package com.osmate.app.data.repository

import com.osmate.app.data.api.OsmateApiClient
import com.osmate.app.data.model.SearchResult

class SearchRepository(
    private val apiClient: OsmateApiClient = OsmateApiClient(),
) {
    suspend fun getBackendHealth(): String {
        return apiClient.getBackendHealth()
    }

    suspend fun search(
        query: String,
        placeName: String,
        radiusM: Int,
    ): SearchResult {
        return apiClient.search(
            query = query,
            placeName = placeName,
            radiusM = radiusM,
        )
    }
}