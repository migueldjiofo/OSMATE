package com.osmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osmate.app.data.api.OsmateApiClient
import com.osmate.app.data.model.SearchResultItem
import com.osmate.app.ui.state.SearchUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val apiClient: OsmateApiClient = OsmateApiClient(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun updateQuery(value: String) {
        _uiState.update { state ->
            state.copy(query = value)
        }
    }

    fun updatePlaceName(value: String) {
        _uiState.update { state ->
            state.copy(placeName = value)
        }
    }

    fun updateRadius(value: String) {
        _uiState.update { state ->
            state.copy(radiusM = value)
        }
    }

    fun applySearchExample(
        query: String,
        placeName: String,
        radiusM: String,
    ) {
        _uiState.update { state ->
            state.copy(
                query = query,
                placeName = placeName,
                radiusM = radiusM,
                backendStatus = "",
                resultText = "",
                resultCount = 0,
                geoJson = "",
                resultItems = emptyList(),
                selectedResult = null,
                selectedResultTitle = "",
                selectedLatitude = null,
                selectedLongitude = null,
                errorMessage = "",
                isLoading = false,
            )
        }
    }

    fun selectResult(item: SearchResultItem) {
        _uiState.update { state ->
            state.copy(
                selectedResult = item,
                selectedResultTitle = item.title,
                selectedLatitude = item.latitude,
                selectedLongitude = item.longitude,
            )
        }
    }

    fun resetResults() {
        _uiState.update { state ->
            state.copy(
                backendStatus = "",
                resultText = "",
                resultCount = 0,
                geoJson = "",
                resultItems = emptyList(),
                selectedResult = null,
                selectedResultTitle = "",
                selectedLatitude = null,
                selectedLongitude = null,
                errorMessage = "",
                isLoading = false,
            )
        }
    }

    fun checkBackend() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    errorMessage = "",
                )
            }

            runCatching {
                apiClient.getBackendHealth()
            }.onSuccess { status ->
                _uiState.update { state ->
                    state.copy(
                        backendStatus = status,
                        errorMessage = "",
                        isLoading = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        errorMessage = createFriendlyErrorMessage(error),
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun createPlan() {
        val currentState = _uiState.value
        val query = currentState.query.trim()
        val placeName = currentState.placeName.trim()
        val radius = validateRadius(currentState.radiusM)

        if (query.isBlank()) {
            showValidationError("Bitte gib eine Suchanfrage ein.")
            return
        }

        if (placeName.isBlank()) {
            showValidationError("Bitte gib einen Ort ein.")
            return
        }

        if (radius == null) {
            showValidationError("Der Radius muss zwischen 100 und 5000 Metern liegen.")
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    errorMessage = "",
                    resultText = "",
                    resultCount = 0,
                    geoJson = "",
                    resultItems = emptyList(),
                    selectedResult = null,
                    selectedResultTitle = "",
                    selectedLatitude = null,
                    selectedLongitude = null,
                )
            }

            runCatching {
                apiClient.search(
                    query = query,
                    placeName = placeName,
                    radiusM = radius,
                )
            }.onSuccess { result ->
                val plan = result.plan
                val tags = plan.osmTags.entries.joinToString(
                    separator = ", ",
                ) { entry ->
                    "${entry.key}=${entry.value}"
                }

                _uiState.update { state ->
                    state.copy(
                        resultText = """
                            Kategorie: ${plan.category}
                            Tags: $tags
                            Treffer: ${result.resultCount}
                            Radius: ${plan.radiusM} m
                            Confidence: ${plan.confidenceScore}
                            Planner: ${plan.plannerType}
                            Summary: ${plan.summary}
                        """.trimIndent(),
                        resultCount = result.resultCount,
                        geoJson = result.geoJson,
                        resultItems = result.items,
                        errorMessage = "",
                        isLoading = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        errorMessage = createFriendlyErrorMessage(error),
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun validateRadius(radiusText: String): Int? {
        val radius = radiusText.trim().toIntOrNull() ?: return null

        if (radius !in 100..5000) {
            return null
        }

        return radius
    }

    private fun showValidationError(message: String) {
        _uiState.update { state ->
            state.copy(
                errorMessage = message,
                isLoading = false,
            )
        }
    }

    private fun createFriendlyErrorMessage(error: Throwable): String {
        val rawMessage = error.message.orEmpty()
        val message = rawMessage.lowercase()

        return when {
            message.contains("failed to connect") ||
                message.contains("connection refused") ||
                message.contains("connectexception") -> {
                "Der Backend-Dienst ist nicht erreichbar. Bitte starte den FastAPI-Server und prüfe http://10.0.2.2:8000."
            }

            message.contains("timeout") ||
                message.contains("timed out") ||
                message.contains("sockettimeoutexception") -> {
                "Die Anfrage hat zu lange gedauert. Bitte versuche es erneut oder reduziere den Radius."
            }

            message.contains("unable to resolve host") ||
                message.contains("unknownhostexception") -> {
                "Es besteht ein Netzwerkproblem. Bitte prüfe die Internetverbindung des Emulators."
            }

            message.contains("http 422") ||
                message.contains("planning_error") -> {
                "Die Suchanfrage konnte nicht in einen gültigen Suchplan übersetzt werden. Bitte formuliere sie einfacher."
            }

            message.contains("geocoding") ||
                message.contains("nominatim") -> {
                "Der eingegebene Ort konnte nicht zuverlässig gefunden werden. Bitte verwende einen genaueren Ortsnamen."
            }

            message.contains("overpass") ||
                message.contains("http 502") ||
                message.contains("http 504") -> {
                "Die OpenStreetMap-Abfrage konnte momentan nicht ausgeführt werden. Bitte versuche es später erneut oder reduziere den Radius."
            }

            message.contains("http 429") ||
                message.contains("rate") -> {
                "Es wurden zu viele Anfragen gestellt. Bitte warte kurz und versuche es erneut."
            }

            rawMessage.isBlank() -> {
                "Es ist ein unbekannter Fehler aufgetreten. Bitte versuche es erneut."
            }

            else -> {
                "Die Anfrage konnte nicht verarbeitet werden. Bitte prüfe Suchanfrage, Ort und Radius."
            }
        }
    }
}