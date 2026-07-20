package com.osmate.app.data.model

data class SearchResult(
    val plan: SearchPlan,
    val resultCount: Int,
    val geoJson: String,
    val items: List<SearchResultItem>,
)