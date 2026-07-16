package com.osmate.app.data.model

data class SearchPlan(
    val category: String,
    val osmTags: Map<String, String>,
    val radiusM: Int,
    val confidenceScore: Double,
    val plannerType: String,
    val summary: String,
    val warnings: List<String>,
)
