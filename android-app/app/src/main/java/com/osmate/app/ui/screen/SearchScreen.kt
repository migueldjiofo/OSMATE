package com.osmate.app.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.animation.core.animateFloatAsState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.osmate.app.data.model.SearchResultItem
import com.osmate.app.domain.location.UserLocation
import com.osmate.app.domain.navigation.TravelMode
import com.osmate.app.ui.map.OsmateMapStyle
import com.osmate.app.ui.map.OsmateMapView
import com.osmate.app.ui.state.SearchUiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

private val OsmatePurple = Color(0xFF7C3AED)
private val OsmatePurpleLight = Color(0xFFF3E8FF)
private val OsmateGreen = Color(0xFF16A34A)
private val OsmateBackground = Color(0xFFF8F9FC)
private val OsmateText = Color(0xFF15162A)
private val OsmateSecondaryText = Color(0xFF66697A)

private enum class BottomSheetTab(
    val label: String,
) {
    Results("Ergebnisse"),
    Details("Details"),
    Route("Route"),
    Agent("Agent"),
}

private enum class BottomSheetLevel(
    val fraction: Float,
) {
    Compact(0.28f),
    Medium(0.52f),
    Expanded(0.88f),
}

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onPlaceNameChange: (String) -> Unit,
    onRadiusChange: (String) -> Unit,
    onCheckBackendClick: () -> Unit,
    onCreatePlanClick: () -> Unit,
    onResetClick: () -> Unit,
    onExampleClick: (String, String, String) -> Unit,
    onResultClick: (SearchResultItem) -> Unit,
    onCalculateRouteClick: (Double?, Double?, Double?, Double?, TravelMode) -> Unit,
    onSearchFromCurrentLocationClick: (Double?, Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userLocation = remember {
        mutableStateOf<UserLocation?>(null)
    }

    val userLocationFocusRequest = remember {
        mutableIntStateOf(0)
    }

    val locationStatus = remember {
        mutableStateOf("")
    }

    val selectedTravelMode = remember {
        mutableStateOf(TravelMode.Walking)
    }

    val selectedMapStyle = remember {
        mutableStateOf(OsmateMapStyle.Standard)
    }

    val is3DEnabled = remember {
        mutableStateOf(false)
    }

    val showMapStyleDialog = remember {
        mutableStateOf(false)
    }

    val zoomRequest = remember {
        mutableIntStateOf(0)
    }

    val zoomDirection = remember {
        mutableIntStateOf(0)
    }

    fun updateUserLocation() {
        coroutineScope.launch {
            locationStatus.value = "Standort wird ermittelt..."

            val location = loadFreshCurrentLocation(context)

            if (location != null) {
                userLocation.value = location
                userLocationFocusRequest.intValue += 1
                locationStatus.value = buildLocationStatus(location)
            } else {
                locationStatus.value =
                    "Keine aktuelle Position verfügbar. Bitte GPS aktivieren oder den Standort im Emulator setzen."
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val fineGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted =
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            updateUserLocation()
        } else {
            locationStatus.value =
                "Standortberechtigung wurde nicht erteilt."
        }
    }

    val hasSearchResults =
        state.resultItems.isNotEmpty() ||
            state.resultText.isNotBlank()

    if (showMapStyleDialog.value) {
        MapStyleSelectionDialog(
            selectedMapStyle = selectedMapStyle.value,
            onMapStyleSelected = { mapStyle ->
                selectedMapStyle.value = mapStyle
                showMapStyleDialog.value = false
            },
            onDismissRequest = {
                showMapStyleDialog.value = false
            },
        )
    }

    if (hasSearchResults) {
        ModernMapScreen(
            state = state,
            userLocation = userLocation.value,
            userLocationFocusRequest = userLocationFocusRequest.intValue,
            selectedTravelMode = selectedTravelMode.value,
            mapStyle = selectedMapStyle.value,
            is3DEnabled = is3DEnabled.value,
            zoomRequest = zoomRequest.intValue,
            zoomDirection = zoomDirection.intValue,
            onTravelModeChange = { mode ->
                selectedTravelMode.value = mode
            },
            onToggle3DClick = {
                is3DEnabled.value = !is3DEnabled.value
            },
            onMapStyleClick = {
                showMapStyleDialog.value = true
            },
            onZoomInClick = {
                zoomDirection.intValue = 1
                zoomRequest.intValue += 1
            },
            onZoomOutClick = {
                zoomDirection.intValue = -1
                zoomRequest.intValue += 1
            },
            onResultClick = onResultClick,
            onCalculateRouteClick = onCalculateRouteClick,
            onBackClick = onResetClick,
            onFocusUserClick = {
                if (hasLocationPermission(context)) {
                    updateUserLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
            },
            modifier = modifier,
        )
    } else {
        ModernSearchScreen(
            state = state,
            userLocation = userLocation.value,
            locationStatus = locationStatus.value,
            onQueryChange = onQueryChange,
            onPlaceNameChange = onPlaceNameChange,
            onRadiusChange = onRadiusChange,
            onCreatePlanClick = onCreatePlanClick,
            onResetClick = onResetClick,
            onExampleClick = onExampleClick,
            onRequestLocationClick = {
                if (hasLocationPermission(context)) {
                    updateUserLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
            },
            onSearchFromCurrentLocationClick = {
                onSearchFromCurrentLocationClick(
                    userLocation.value?.latitude,
                    userLocation.value?.longitude,
                )
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun ModernSearchScreen(
    state: SearchUiState,
    userLocation: UserLocation?,
    locationStatus: String,
    onQueryChange: (String) -> Unit,
    onPlaceNameChange: (String) -> Unit,
    onRadiusChange: (String) -> Unit,
    onCreatePlanClick: () -> Unit,
    onResetClick: () -> Unit,
    onExampleClick: (String, String, String) -> Unit,
    onRequestLocationClick: () -> Unit,
    onSearchFromCurrentLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OsmateBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ModernHeader()

            MainSearchField(
                query = state.query,
                onQueryChange = onQueryChange,
            )

            ModernQuickExamples(
                onExampleClick = onExampleClick,
            )

            SearchConfigurationCard(
                state = state,
                userLocation = userLocation,
                onPlaceNameChange = onPlaceNameChange,
                onRadiusChange = onRadiusChange,
                onRequestLocationClick = onRequestLocationClick,
                onSearchFromCurrentLocationClick = onSearchFromCurrentLocationClick,
                onCreatePlanClick = onCreatePlanClick,
                onResetClick = onResetClick,
            )

            if (userLocation != null || locationStatus.isNotBlank()) {
                LocationStatusCard(
                    userLocation = userLocation,
                    locationStatus = locationStatus,
                    onRefreshClick = onRequestLocationClick,
                )
            }

            if (state.isLoading) {
                ModernLoadingCard()
            }

            if (state.errorMessage.isNotBlank()) {
                ModernErrorCard(
                    message = state.errorMessage,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModernHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "OSMATE",
            color = OsmateText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Agentischer räumlicher Assistent\nfür OpenStreetMap",
            color = OsmateSecondaryText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun MainSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 5.dp,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            placeholder = {
                Text("Was suchst du?")
            },
            leadingIcon = {
                Text(
                    text = "⌕",
                    color = OsmatePurple,
                    fontSize = 28.sp,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
        )
    }
}

@Composable
private fun ModernQuickExamples(
    onExampleClick: (String, String, String) -> Unit,
) {
    val examples = listOf(
        SearchExample(
            icon = "☕",
            label = "Cafés\nmit Terrasse",
            query = "Finde Cafes mit Terrasse",
            placeName = "Berlin Alexanderplatz",
            radiusM = "1000",
        ),
        SearchExample(
            icon = "▣",
            label = "Spätis\nin der Nähe",
            query = "Finde Spaetis in der Naehe",
            placeName = "Berlin Kreuzberg",
            radiusM = "1200",
        ),
        SearchExample(
            icon = "♟",
            label = "Spielplätze\nmit Wasser",
            query = "Finde Spielplaetze mit Wasserspielen",
            placeName = "Berlin Mitte",
            radiusM = "1500",
        ),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Schnellbeispiele",
                color = OsmateText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "Mehr",
                color = OsmateSecondaryText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            examples.forEach { example ->
                Surface(
                    modifier = Modifier.width(145.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    onClick = {
                        onExampleClick(
                            example.query,
                            example.placeName,
                            example.radiusM,
                        )
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = OsmatePurpleLight,
                        ) {
                            Text(
                                text = example.icon,
                                modifier = Modifier.padding(9.dp),
                                fontSize = 18.sp,
                            )
                        }

                        Text(
                            text = example.label,
                            color = OsmateText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchConfigurationCard(
    state: SearchUiState,
    userLocation: UserLocation?,
    onPlaceNameChange: (String) -> Unit,
    onRadiusChange: (String) -> Unit,
    onRequestLocationClick: () -> Unit,
    onSearchFromCurrentLocationClick: () -> Unit,
    onCreatePlanClick: () -> Unit,
    onResetClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Suche konfigurieren",
                color = OsmateText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = state.placeName,
                onValueChange = onPlaceNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Ort")
                },
                placeholder = {
                    Text("Berlin Alexanderplatz")
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )

            OutlinedTextField(
                value = state.radiusM,
                onValueChange = onRadiusChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Radius in Metern")
                },
                suffix = {
                    Text("m")
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )

            Text(
                text = "Verwende",
                color = OsmateText,
                style = MaterialTheme.typography.titleSmall,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onRequestLocationClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userLocation != null) {
                            OsmatePurple
                        } else {
                            OsmatePurpleLight
                        },
                        contentColor = if (userLocation != null) {
                            Color.White
                        } else {
                            OsmatePurple
                        },
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("⌖  Meine Position")
                }

                OutlinedButton(
                    onClick = onSearchFromCurrentLocationClick,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("In der Nähe")
                }
            }

            Button(
                onClick = onCreatePlanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OsmatePurple,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = "⌕  Suchen",
                    fontWeight = FontWeight.SemiBold,
                )
            }

            OutlinedButton(
                onClick = onResetClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("↻  Zurücksetzen")
            }
        }
    }
}

@Composable
private fun LocationStatusCard(
    userLocation: UserLocation?,
    locationStatus: String,
    onRefreshClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFE8F8ED),
            ) {
                Text(
                    text = "⌖",
                    modifier = Modifier.padding(10.dp),
                    color = OsmateGreen,
                    fontSize = 22.sp,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Meine Position",
                    color = OsmateGreen,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    text = if (userLocation != null) {
                        "${userLocation.latitude}, ${userLocation.longitude}"
                    } else {
                        locationStatus
                    },
                    color = OsmateSecondaryText,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            OutlinedButton(
                onClick = onRefreshClick,
                shape = CircleShape,
            ) {
                Text("↻")
            }
        }
    }
}

@Composable
private fun ModernMapScreen(
    state: SearchUiState,
    userLocation: UserLocation?,
    userLocationFocusRequest: Int,
    selectedTravelMode: TravelMode,
    mapStyle: OsmateMapStyle,
    is3DEnabled: Boolean,
    zoomRequest: Int,
    zoomDirection: Int,
    onTravelModeChange: (TravelMode) -> Unit,
    onToggle3DClick: () -> Unit,
    onMapStyleClick: () -> Unit,
    onZoomInClick: () -> Unit,
    onZoomOutClick: () -> Unit,
    onResultClick: (SearchResultItem) -> Unit,
    onCalculateRouteClick: (Double?, Double?, Double?, Double?, TravelMode) -> Unit,
    onBackClick: () -> Unit,
    onFocusUserClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        OsmateMapView(
            geoJson = state.geoJson,
            routeGeoJson = state.routeGeoJson,
            selectedLatitude = state.selectedLatitude,
            selectedLongitude = state.selectedLongitude,
            userLatitude = userLocation?.latitude,
            userLongitude = userLocation?.longitude,
            userLocationFocusRequest = userLocationFocusRequest,
            mapStyle = mapStyle,
            is3DEnabled = is3DEnabled,
            zoomRequest = zoomRequest,
            zoomDirection = zoomDirection,
            onSelectedPointClick = {},
            modifier = Modifier.fillMaxSize(),
        )

        MapTopControls(
            query = state.query,
            onBackClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(14.dp),
        )

        MapSideControls(
            is3DEnabled = is3DEnabled,
            onToggle3DClick = onToggle3DClick,
            onMapStyleClick = onMapStyleClick,
            onZoomInClick = onZoomInClick,
            onZoomOutClick = onZoomOutClick,
            onFocusUserClick = onFocusUserClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
        )

        ResultsBottomSheet(
            state = state,
            userLocation = userLocation,
            selectedTravelMode = selectedTravelMode,
            onTravelModeChange = onTravelModeChange,
            onResultClick = onResultClick,
            onCalculateRouteClick = onCalculateRouteClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
    }
}

@Composable
private fun MapTopControls(
    query: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 6.dp,
            onClick = onBackClick,
        ) {
            Text(
                text = "‹",
                modifier = Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 10.dp,
                ),
                fontSize = 30.sp,
                color = OsmateText,
            )
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 6.dp,
        ) {
            Text(
                text = query,
                modifier = Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 14.dp,
                ),
                color = OsmateText,
                maxLines = 1,
                fontWeight = FontWeight.Medium,
            )
        }

        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 6.dp,
        ) {
            Text(
                text = "☷",
                modifier = Modifier.padding(14.dp),
                color = OsmateText,
                fontSize = 20.sp,
            )
        }
    }
}

@Composable
private fun MapSideControls(
    is3DEnabled: Boolean,
    onToggle3DClick: () -> Unit,
    onMapStyleClick: () -> Unit,
    onZoomInClick: () -> Unit,
    onZoomOutClick: () -> Unit,
    onFocusUserClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MapControlButton(
            label = if (is3DEnabled) {
                "2D"
            } else {
                "3D"
            },
            highlighted = is3DEnabled,
            onClick = onToggle3DClick,
        )

        MapControlButton(
            label = "▱",
            onClick = onMapStyleClick,
        )

        MapControlButton(
            label = "+",
            onClick = onZoomInClick,
        )

        MapControlButton(
            label = "−",
            onClick = onZoomOutClick,
        )

        MapControlButton(
            label = "⌖",
            highlighted = true,
            onClick = onFocusUserClick,
        )
    }
}

@Composable
private fun MapStyleSelectionDialog(
    selectedMapStyle: OsmateMapStyle,
    onMapStyleSelected: (OsmateMapStyle) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Kartenstil auswählen")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MapStyleOptionButton(
                    label = "OpenStreetMap Standard",
                    selected = selectedMapStyle == OsmateMapStyle.Standard,
                    onClick = {
                        onMapStyleSelected(OsmateMapStyle.Standard)
                    },
                )

                MapStyleOptionButton(
                    label = "Humanitarian",
                    selected = selectedMapStyle == OsmateMapStyle.Humanitarian,
                    onClick = {
                        onMapStyleSelected(OsmateMapStyle.Humanitarian)
                    },
                )
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismissRequest,
            ) {
                Text("Schließen")
            }
        },
    )
}

@Composable
private fun MapStyleOptionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = OsmatePurple,
            ),
        ) {
            Text(label)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(label)
        }
    }
}
@Composable
private fun MapControlButton(
    label: String,
    highlighted: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = if (highlighted) {
            OsmatePurple
        } else {
            Color.White
        },
        contentColor = if (highlighted) {
            Color.White
        } else {
            OsmateText
        },
        shadowElevation = 6.dp,
        onClick = onClick,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ResultsBottomSheet(
    state: SearchUiState,
    userLocation: UserLocation?,
    selectedTravelMode: TravelMode,
    onTravelModeChange: (TravelMode) -> Unit,
    onResultClick: (SearchResultItem) -> Unit,
    onCalculateRouteClick: (
        Double?,
        Double?,
        Double?,
        Double?,
        TravelMode,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedTab = remember {
        mutableStateOf(BottomSheetTab.Results)
    }

    val sheetLevel = remember {
        mutableStateOf(BottomSheetLevel.Medium)
    }

    val dragDistance = remember {
        mutableStateOf(0f)
    }

    val animatedSheetFraction by animateFloatAsState(
        targetValue = sheetLevel.value.fraction,
        label = "bottomSheetHeight",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(animatedSheetFraction),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
        ),
        color = Color.White,
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 18.dp,
                    vertical = 12.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .pointerInput(sheetLevel.value) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                dragDistance.value = 0f
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                dragDistance.value += dragAmount
                            },
                            onDragEnd = {
                                val threshold = 70f

                                sheetLevel.value = when {
                                    dragDistance.value < -threshold -> {
                                        when (sheetLevel.value) {
                                            BottomSheetLevel.Compact ->
                                                BottomSheetLevel.Medium

                                            BottomSheetLevel.Medium ->
                                                BottomSheetLevel.Expanded

                                            BottomSheetLevel.Expanded ->
                                                BottomSheetLevel.Expanded
                                        }
                                    }

                                    dragDistance.value > threshold -> {
                                        when (sheetLevel.value) {
                                            BottomSheetLevel.Compact ->
                                                BottomSheetLevel.Compact

                                            BottomSheetLevel.Medium ->
                                                BottomSheetLevel.Compact

                                            BottomSheetLevel.Expanded ->
                                                BottomSheetLevel.Medium
                                        }
                                    }

                                    else -> sheetLevel.value
                                }

                                dragDistance.value = 0f
                            },
                            onDragCancel = {
                                dragDistance.value = 0f
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier
                        .width(48.dp)
                        .height(5.dp),
                    shape = CircleShape,
                    color = Color(0xFFD5D6DC),
                ) {}
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "${state.resultCount} Ergebnisse",
                        color = OsmateText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = state.query,
                        color = OsmateSecondaryText,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                    )
                }

                SheetLevelIndicator(
                    sheetLevel = sheetLevel.value,
                    onClick = {
                        sheetLevel.value = when (sheetLevel.value) {
                            BottomSheetLevel.Compact ->
                                BottomSheetLevel.Medium

                            BottomSheetLevel.Medium ->
                                BottomSheetLevel.Expanded

                            BottomSheetLevel.Expanded ->
                                BottomSheetLevel.Compact
                        }
                    },
                )
            }

            BottomSheetTabBar(
                selectedTab = selectedTab.value,
                onTabSelected = { tab ->
                    selectedTab.value = tab

                    if (sheetLevel.value == BottomSheetLevel.Compact) {
                        sheetLevel.value = BottomSheetLevel.Medium
                    }
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when (selectedTab.value) {
                    BottomSheetTab.Results -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SearchResultList(
                                resultCount = state.resultCount,
                                items = state.resultItems,
                                selectedTitle = state.selectedResultTitle,
                                showEmptyState = state.resultText.isNotBlank(),
                                onItemClick = { item ->
                                    onResultClick(item)
                                    selectedTab.value =
                                        BottomSheetTab.Details
                                    sheetLevel.value =
                                        BottomSheetLevel.Medium
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )

                            if (state.errorMessage.isNotBlank()) {
                                ModernErrorCard(
                                    message = state.errorMessage,
                                )
                            }
                        }
                    }

                    BottomSheetTab.Details -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (state.selectedResult != null) {
                                SelectedResultDetail(
                                    item = state.selectedResult,
                                    userLatitude = userLocation?.latitude,
                                    userLongitude = userLocation?.longitude,
                                    selectedTravelMode =
                                        selectedTravelMode,
                                    onTravelModeChange =
                                        onTravelModeChange,
                                    routeDistanceText =
                                        state.routeDistanceText,
                                    routeDurationText =
                                        state.routeDurationText,
                                    routeProvider =
                                        state.routeProvider,
                                    routeWarnings =
                                        state.routeWarnings,
                                    isRouteLoading =
                                        state.isRouteLoading,
                                    onCalculateRouteClick =
                                        onCalculateRouteClick,
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                )
                            } else {
                                BottomSheetEmptyState(
                                    title = "Kein Ergebnis ausgewählt",
                                    message =
                                        "Wähle zuerst einen Ort aus der Ergebnisliste aus.",
                                )
                            }
                        }
                    }

                    BottomSheetTab.Route -> {
                        RouteTabContent(
                            state = state,
                            userLocation = userLocation,
                            selectedTravelMode =
                                selectedTravelMode,
                            onTravelModeChange =
                                onTravelModeChange,
                            onCalculateRouteClick =
                                onCalculateRouteClick,
                        )
                    }

                    BottomSheetTab.Agent -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (state.resultText.isNotBlank()) {
                                AgentPlanCard(
                                    body = state.resultText,
                                )
                            } else {
                                BottomSheetEmptyState(
                                    title = "Keine Agentenanalyse",
                                    message =
                                        "Für diese Suche liegt noch keine Agentenanalyse vor.",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetLevelIndicator(
    sheetLevel: BottomSheetLevel,
    onClick: () -> Unit,
) {
    val label = when (sheetLevel) {
        BottomSheetLevel.Compact -> "Kompakt"
        BottomSheetLevel.Medium -> "Mittel"
        BottomSheetLevel.Expanded -> "Voll"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = OsmatePurpleLight,
        onClick = onClick,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 7.dp,
            ),
            color = OsmatePurple,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
@Composable
private fun BottomSheetTabBar(
    selectedTab: BottomSheetTab,
    onTabSelected: (BottomSheetTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BottomSheetTab.entries.forEach { tab ->
            BottomSheetTabButton(
                label = tab.label,
                selected = tab == selectedTab,
                onClick = {
                    onTabSelected(tab)
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BottomSheetTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(18.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 4.dp,
                vertical = 8.dp,
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = OsmatePurple,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = label,
                maxLines = 1,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(18.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 4.dp,
                vertical = 8.dp,
            ),
        ) {
            Text(
                text = label,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun RouteTabContent(
    state: SearchUiState,
    userLocation: UserLocation?,
    selectedTravelMode: TravelMode,
    onTravelModeChange: (TravelMode) -> Unit,
    onCalculateRouteClick: (
        Double?,
        Double?,
        Double?,
        Double?,
        TravelMode,
    ) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Navigation",
            color = OsmateText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = if (state.selectedResultTitle.isNotBlank()) {
                state.selectedResultTitle
            } else {
                "Kein Ziel ausgewählt"
            },
            color = OsmateSecondaryText,
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = "Verkehrsmittel",
            color = OsmateText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TravelModeButton(
                label = "Zu Fuß",
                mode = TravelMode.Walking,
                selectedTravelMode = selectedTravelMode,
                onTravelModeChange = onTravelModeChange,
            )

            TravelModeButton(
                label = "Fahrrad",
                mode = TravelMode.Bicycle,
                selectedTravelMode = selectedTravelMode,
                onTravelModeChange = onTravelModeChange,
            )

            TravelModeButton(
                label = "Auto",
                mode = TravelMode.Car,
                selectedTravelMode = selectedTravelMode,
                onTravelModeChange = onTravelModeChange,
            )
        }

        if (
            state.routeDistanceText.isNotBlank() ||
            state.routeDurationText.isNotBlank()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFAFAFF),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Routenübersicht",
                        color = OsmateText,
                        fontWeight = FontWeight.Bold,
                    )

                    if (state.routeDistanceText.isNotBlank()) {
                        Text(
                            text =
                                "Entfernung: ${state.routeDistanceText}",
                            color = OsmateSecondaryText,
                        )
                    }

                    if (state.routeDurationText.isNotBlank()) {
                        Text(
                            text =
                                "Dauer: ${state.routeDurationText}",
                            color = OsmateSecondaryText,
                        )
                    }

                    if (state.routeProvider.isNotBlank()) {
                        Text(
                            text =
                                "Routingdienst: ${state.routeProvider}",
                            color = OsmateSecondaryText,
                            style =
                                MaterialTheme.typography.bodySmall,
                        )
                    }

                    state.routeWarnings.forEach { warning ->
                        Text(
                            text = warning,
                            color = Color(0xFF9A6700),
                            style =
                                MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                onCalculateRouteClick(
                    userLocation?.latitude,
                    userLocation?.longitude,
                    state.selectedLatitude,
                    state.selectedLongitude,
                    selectedTravelMode,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled =
                userLocation != null &&
                    state.selectedLatitude != null &&
                    state.selectedLongitude != null &&
                    !state.isRouteLoading,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OsmatePurple,
                contentColor = Color.White,
            ),
        ) {
            if (state.isRouteLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .width(22.dp)
                        .height(22.dp),
                )
            } else {
                Text(
                    text = "Route berechnen",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        if (userLocation == null) {
            Text(
                text =
                    "Aktiviere zuerst deinen Standort, um eine Route zu berechnen.",
                color = OsmateSecondaryText,
                style = MaterialTheme.typography.bodySmall,
            )
        } else if (
            state.selectedLatitude == null ||
            state.selectedLongitude == null
        ) {
            Text(
                text =
                    "Wähle zuerst ein Ziel aus der Ergebnisliste aus.",
                color = OsmateSecondaryText,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun TravelModeButton(
    label: String,
    mode: TravelMode,
    selectedTravelMode: TravelMode,
    onTravelModeChange: (TravelMode) -> Unit,
) {
    if (mode == selectedTravelMode) {
        Button(
            onClick = {
                onTravelModeChange(mode)
            },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OsmatePurple,
            ),
        ) {
            Text(label)
        }
    } else {
        OutlinedButton(
            onClick = {
                onTravelModeChange(mode)
            },
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(label)
        }
    }
}

@Composable
private fun BottomSheetEmptyState(
    title: String,
    message: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAFAFF),
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                color = OsmateText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = message,
                color = OsmateSecondaryText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
@Composable
private fun AgentPlanCard(
    body: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAFAFF),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "AgentPlan",
                    color = OsmateText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE8F8ED),
                ) {
                    Text(
                        text = "rule_based",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 4.dp,
                        ),
                        color = OsmateGreen,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            Text(
                text = body,
                color = OsmateSecondaryText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ModernLoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                color = OsmatePurple,
            )

            Text(
                text = "OSMATE analysiert die Anfrage...",
                color = OsmateText,
            )
        }
    }
}

@Composable
private fun ModernErrorCard(
    message: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF1F2),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Hinweis",
                color = Color(0xFFBE123C),
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = message,
                color = Color(0xFF881337),
            )
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fineLocationGranted = context.checkSelfPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    val coarseLocationGranted = context.checkSelfPermission(
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    return fineLocationGranted || coarseLocationGranted
}

@SuppressLint("MissingPermission")
private suspend fun loadFreshCurrentLocation(
    context: Context,
): UserLocation? {
    if (!hasLocationPermission(context)) {
        return null
    }

    val locationManager = context.getSystemService(
        Context.LOCATION_SERVICE,
    ) as? LocationManager ?: return null

    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    ).filter { provider ->
        runCatching {
            locationManager.isProviderEnabled(provider)
        }.getOrDefault(false)
    }

    val freshLocation = providers.firstNotNullOfOrNull { provider ->
        withTimeoutOrNull(6000L) {
            requestSingleLocationUpdate(
                locationManager = locationManager,
                provider = provider,
            )
        }
    }

    return freshLocation?.toUserLocation()
        ?: loadLastKnownLocation(locationManager)?.toUserLocation()
}

@SuppressLint("MissingPermission")
private suspend fun requestSingleLocationUpdate(
    locationManager: LocationManager,
    provider: String,
): Location? {
    return suspendCancellableCoroutine { continuation ->
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)

                if (continuation.isActive) {
                    continuation.resume(location)
                }
            }

            override fun onProviderDisabled(provider: String) = Unit

            override fun onProviderEnabled(provider: String) = Unit

            override fun onStatusChanged(
                provider: String?,
                status: Int,
                extras: Bundle?,
            ) = Unit
        }

        runCatching {
            locationManager.requestLocationUpdates(
                provider,
                0L,
                0.0f,
                listener,
                Looper.getMainLooper(),
            )
        }.onFailure {
            locationManager.removeUpdates(listener)

            if (continuation.isActive) {
                continuation.resume(null)
            }
        }

        continuation.invokeOnCancellation {
            locationManager.removeUpdates(listener)
        }
    }
}

@SuppressLint("MissingPermission")
private fun loadLastKnownLocation(
    locationManager: LocationManager,
): Location? {
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    )

    return providers.mapNotNull { provider ->
        runCatching {
            locationManager.getLastKnownLocation(provider)
        }.getOrNull()
    }.maxByOrNull { location ->
        location.time
    }
}

private fun Location.toUserLocation(): UserLocation {
    return UserLocation(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracy,
        provider = provider,
    )
}

private fun buildLocationStatus(
    location: UserLocation,
): String {
    val accuracyText = location.accuracyMeters?.let { accuracy ->
        "Genauigkeit: ${accuracy.toInt()} m"
    } ?: "Genauigkeit: unbekannt"

    val providerText = location.provider?.let { provider ->
        "Quelle: $provider"
    } ?: "Quelle: unbekannt"

    return "${location.latitude}, ${location.longitude} · $accuracyText · $providerText"
}

private data class SearchExample(
    val icon: String,
    val label: String,
    val query: String,
    val placeName: String,
    val radiusM: String,
)





