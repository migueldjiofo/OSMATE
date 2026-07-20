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
                geoJson = "",
                resultItems = emptyList(),
                resultCount = 0,
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
                geoJson = "",
                resultItems = emptyList(),
                resultCount = 0,
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
                        isLoading = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        errorMessage = error.message.orEmpty(),
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
                    geoJson = "",
                    resultItems = emptyList(),
                resultCount = 0,
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
                        geoJson = result.geoJson,
                        resultItems = result.items,
                        resultCount = result.resultCount,
                        isLoading = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        errorMessage = error.message.orEmpty(),
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
}