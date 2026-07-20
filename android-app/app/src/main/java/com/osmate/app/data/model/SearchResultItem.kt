package com.osmate.app.data.model

data class SearchResultItem(
    val title: String,
    val subtitle: String,
    val primaryTag: String,
    val openingHours: String,
    val latitude: Double?,
    val longitude: Double?,
)