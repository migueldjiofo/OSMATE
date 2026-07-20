package com.osmate.app.ui.state

import com.osmate.app.data.model.SearchResultItem

data class SearchUiState(
    val query: String = "Finde Cafes mit Terrasse",
    val placeName: String = "Berlin Alexanderplatz",
    val radiusM: String = "1000",
    val backendStatus: String = "",
    val resultText: String = "",
    val geoJson: String = "",
    val resultItems: List<SearchResultItem> = emptyList(),
    val errorMessage: String = "",
    val isLoading: Boolean = false,
)
