package com.osmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osmate.app.data.model.SearchResult
import com.osmate.app.data.model.SearchResultItem
import com.osmate.app.data.repository.SearchRepository
import com.osmate.app.domain.navigation.TravelMode
import com.osmate.app.domain.validation.SearchInputValidator
import com.osmate.app.ui.error.ErrorMessageMapper
import com.osmate.app.ui.state.SearchUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: SearchRepository = SearchRepository(),
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
                routeGeoJson = "",
                routeDistanceText = "",
                routeDurationText = "",
                routeProvider = "",
                routeWarnings = emptyList(),
                errorMessage = "",
                isLoading = false,
                isRouteLoading = false,
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
                routeGeoJson = "",
                routeDistanceText = "",
                routeDurationText = "",
                routeProvider = "",
                routeWarnings = emptyList(),
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
                routeGeoJson = "",
                routeDistanceText = "",
                routeDurationText = "",
                routeProvider = "",
                routeWarnings = emptyList(),
                errorMessage = "",
                isLoading = false,
                isRouteLoading = false,
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
                repository.getBackendHealth()
            }.onSuccess { status ->
                _uiState.update { state ->
                    state.copy(
                        backendStatus = status,
                        errorMessage = "",
                        isLoading = false,
                    )
                }
            }.onFailure { error ->
                showError(error)
            }
        }
    }

    fun createPlan() {
        val currentState = _uiState.value

        val validation = SearchInputValidator.validate(
            query = currentState.query,
            placeName = currentState.placeName,
            radiusText = currentState.radiusM,
        )

        if (validation.errorMessage != null || validation.radiusM == null) {
            showValidationError(validation.errorMessage.orEmpty())
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
                    routeGeoJson = "",
                    routeDistanceText = "",
                    routeDurationText = "",
                    routeProvider = "",
                    routeWarnings = emptyList(),
                    isRouteLoading = false,
                )
            }

            runCatching {
                repository.search(
                    query = validation.query,
                    placeName = validation.placeName,
                    latitude = null,
                    longitude = null,
                    radiusMeters = validation.radiusM,
                    limit = 20,
                )
            }.onSuccess { result ->
                applySearchResult(result)
            }.onFailure { error ->
                showError(error)
            }
        }
    }

    fun searchFromCurrentLocation(latitude: Double?, longitude: Double?) {
        if (latitude == null || longitude == null) {
            _uiState.update { state ->
                state.copy(
                    errorMessage = "Bitte ermittle zuerst deine aktuelle Position.",
                    isLoading = false,
                )
            }
            return
        }

        val currentState = _uiState.value
        val radiusMeters = currentState.radiusM.toIntOrNull()

        if (currentState.query.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    errorMessage = "Bitte gib zuerst eine Suchanfrage ein.",
                    isLoading = false,
                )
            }
            return
        }

        if (radiusMeters == null || radiusMeters <= 0) {
            _uiState.update { state ->
                state.copy(
                    errorMessage = "Bitte gib einen gueltigen Radius ein.",
                    isLoading = false,
                )
            }
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
                    routeGeoJson = "",
                    routeDistanceText = "",
                    routeDurationText = "",
                    routeProvider = "",
                    routeWarnings = emptyList(),
                    isRouteLoading = false,
                )
            }

            runCatching {
                repository.search(
                    query = currentState.query,
                    placeName = null,
                    latitude = latitude,
                    longitude = longitude,
                    radiusMeters = radiusMeters,
                    limit = 20,
                )
            }.onSuccess { result ->
                applySearchResult(result)
            }.onFailure { error ->
                showError(error)
            }
        }
    }

    fun calculateRoute(
        startLatitude: Double?,
        startLongitude: Double?,
        destinationLatitude: Double?,
        destinationLongitude: Double?,
        mode: TravelMode,
    ) {
        if (
            startLatitude == null ||
            startLongitude == null ||
            destinationLatitude == null ||
            destinationLongitude == null
        ) {
            _uiState.update { state ->
                state.copy(
                    errorMessage = "Fuer die Route werden aktuelle Position und Zielkoordinate benoetigt.",
                    isRouteLoading = false,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isRouteLoading = true,
                    errorMessage = "",
                )
            }

            runCatching {
                repository.calculateRoute(
                    startLatitude = startLatitude,
                    startLongitude = startLongitude,
                    destinationLatitude = destinationLatitude,
                    destinationLongitude = destinationLongitude,
                    mode = mode.apiValue,
                )
            }.onSuccess { route ->
                _uiState.update { state ->
                    state.copy(
                        routeGeoJson = route.geoJson,
                        routeDistanceText = route.distanceText,
                        routeDurationText = route.durationText,
                        routeProvider = route.provider,
                        routeWarnings = route.warnings,
                        isRouteLoading = false,
                        errorMessage = "",
                    )
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        errorMessage = ErrorMessageMapper.fromThrowable(error),
                        isRouteLoading = false,
                    )
                }
            }
        }
    }

    private fun applySearchResult(result: SearchResult) {
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
    }

    private fun showValidationError(message: String) {
        _uiState.update { state ->
            state.copy(
                errorMessage = message,
                isLoading = false,
            )
        }
    }

    private fun showError(error: Throwable) {
        _uiState.update { state ->
            state.copy(
                errorMessage = ErrorMessageMapper.fromThrowable(error),
                isLoading = false,
            )
        }
    }
}