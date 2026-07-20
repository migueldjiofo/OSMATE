package com.osmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osmate.app.data.api.OsmateApiClient
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

    fun checkBackend() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = "")
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
        val radius = currentState.radiusM.toIntOrNull() ?: 1000

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    errorMessage = "",
                    resultText = "",
                    geoJson = "",
                    resultItems = emptyList(),
                )
            }

            runCatching {
                apiClient.search(
                    query = currentState.query,
                    placeName = currentState.placeName,
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
}