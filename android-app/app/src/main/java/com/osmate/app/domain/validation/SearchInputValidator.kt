package com.osmate.app.domain.validation

object SearchInputValidator {
    fun validate(
        query: String,
        placeName: String,
        radiusText: String,
    ): SearchValidationResult {
        val normalizedQuery = query.trim()
        val normalizedPlaceName = placeName.trim()
        val radius = radiusText.trim().toIntOrNull()

        if (normalizedQuery.isBlank()) {
            return SearchValidationResult(
                query = normalizedQuery,
                placeName = normalizedPlaceName,
                radiusM = null,
                errorMessage = "Bitte gib eine Suchanfrage ein.",
            )
        }

        if (normalizedPlaceName.isBlank()) {
            return SearchValidationResult(
                query = normalizedQuery,
                placeName = normalizedPlaceName,
                radiusM = null,
                errorMessage = "Bitte gib einen Ort ein.",
            )
        }

        if (radius == null || radius !in 100..5000) {
            return SearchValidationResult(
                query = normalizedQuery,
                placeName = normalizedPlaceName,
                radiusM = null,
                errorMessage = "Der Radius muss zwischen 100 und 5000 Metern liegen.",
            )
        }

        return SearchValidationResult(
            query = normalizedQuery,
            placeName = normalizedPlaceName,
            radiusM = radius,
            errorMessage = null,
        )
    }
}

data class SearchValidationResult(
    val query: String,
    val placeName: String,
    val radiusM: Int?,
    val errorMessage: String?,
)