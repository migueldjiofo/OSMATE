package com.osmate.app.data.model

data class SearchResultItem(
    val title: String,
    val subtitle: String,
    val primaryTag: String,
    val openingHours: String,
    val latitude: Double?,
    val longitude: Double?,
    val cuisine: String = "",
    val website: String = "",
    val phone: String = "",
    val brand: String = "",
    val operator: String = "",
    val outdoorSeating: String = "",
    val internetAccess: String = "",
    val wheelchair: String = "",
    val takeaway: String = "",
    val allTags: Map<String, String> = emptyMap(),
)