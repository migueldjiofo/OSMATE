package com.osmate.app.ui.state

data class SearchUiState(
    val query: String = "Finde Cafes mit Terrasse",
    val placeName: String = "Berlin Alexanderplatz",
    val radiusM: String = "1000",
    val backendStatus: String = "",
    val resultText: String = "",
    val geoJson: String = "",
    val errorMessage: String = "",
    val isLoading: Boolean = false,
)
