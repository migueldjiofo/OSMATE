package com.osmate.app.ui.state

import com.osmate.app.data.model.SearchResultItem

data class SearchUiState(
    val query: String = "Finde Cafes mit Terrasse",
    val placeName: String = "Berlin Alexanderplatz",
    val radiusM: String = "1000",
    val backendStatus: String = "",
    val resultText: String = "",
    val resultCount: Int = 0,
    val geoJson: String = "",
    val routeGeoJson: String = "",
    val routeDistanceText: String = "",
    val routeDurationText: String = "",
    val routeProvider: String = "",
    val routeWarnings: List<String> = emptyList(),
    val resultItems: List<SearchResultItem> = emptyList(),
    val selectedResult: SearchResultItem? = null,
    val selectedResultTitle: String = "",
    val selectedLatitude: Double? = null,
    val selectedLongitude: Double? = null,
    val errorMessage: String = "",
    val isLoading: Boolean = false,
    val isRouteLoading: Boolean = false,
)