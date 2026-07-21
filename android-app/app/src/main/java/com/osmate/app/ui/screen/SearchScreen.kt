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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.osmate.app.data.model.SearchResultItem
import com.osmate.app.domain.location.UserLocation
import com.osmate.app.domain.navigation.TravelMode
import com.osmate.app.ui.map.OsmateMapView
import com.osmate.app.ui.state.SearchUiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

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

    fun updateUserLocation() {
        coroutineScope.launch {
            locationStatus.value = "Standort wird ermittelt..."

            val location = loadFreshCurrentLocation(context)

            if (location != null) {
                userLocation.value = location
                userLocationFocusRequest.intValue += 1
                locationStatus.value = buildLocationStatus(location)
            } else {
                locationStatus.value = "Keine aktuelle Position verfÃ¼gbar. Bitte Standort im Emulator setzen oder GPS aktivieren."
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            updateUserLocation()
        } else {
            locationStatus.value = "Standortberechtigung wurde nicht erteilt."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "OSMATE",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Agentischer r\u00e4umlicher Assistent f\u00fcr OpenStreetMap",
            style = MaterialTheme.typography.bodyMedium,
        )

        QuickExamples(
            onExampleClick = onExampleClick,
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Suchanfrage")
            },
            singleLine = false,
        )

        OutlinedTextField(
            value = state.placeName,
            onValueChange = onPlaceNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Ort")
            },
            singleLine = true,
        )

        OutlinedTextField(
            value = state.radiusM,
            onValueChange = onRadiusChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Radius in Metern")
            },
            singleLine = true,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onCheckBackendClick,
                enabled = !state.isLoading,
            ) {
                Text("Backend pr\u00fcfen")
            }

            Button(
                onClick = onCreatePlanClick,
                enabled = !state.isLoading,
            ) {
                Text("Suchen")
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onResetClick,
                enabled = !state.isLoading,
            ) {
                Text("Zur\u00fccksetzen")
            }

            OutlinedButton(
                onClick = {
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
                enabled = !state.isLoading,
            ) {
                Text("Meine Position")
            }
        }

        OsmateMapView(
            geoJson = state.geoJson,
            selectedLatitude = state.selectedLatitude,
            selectedLongitude = state.selectedLongitude,
            userLatitude = userLocation.value?.latitude,
            userLongitude = userLocation.value?.longitude,
            userLocationFocusRequest = userLocationFocusRequest.intValue,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
        )

        if (locationStatus.value.isNotBlank()) {
            InfoCard(
                title = "Standort",
                body = locationStatus.value,
            )
        }

        if (state.isLoading) {
            LoadingCard()
        }

        SelectedResultDetail(
            item = state.selectedResult,
            userLatitude = userLocation.value?.latitude,
            userLongitude = userLocation.value?.longitude,
            selectedTravelMode = selectedTravelMode.value,
            onTravelModeChange = { mode ->
                selectedTravelMode.value = mode
            },
            modifier = Modifier.fillMaxWidth(),
        )

        SearchResultList(
            resultCount = state.resultCount,
            items = state.resultItems,
            selectedTitle = state.selectedResultTitle,
            showEmptyState = state.resultText.isNotBlank(),
            onItemClick = onResultClick,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.backendStatus.isNotBlank()) {
            InfoCard(
                title = "Backend",
                body = state.backendStatus,
            )
        }

        if (state.resultText.isNotBlank()) {
            InfoCard(
                title = "AgentPlan",
                body = state.resultText,
            )
        }

        if (state.errorMessage.isNotBlank()) {
            ErrorCard(
                message = state.errorMessage,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun QuickExamples(
    onExampleClick: (String, String, String) -> Unit,
) {
    val examples = listOf(
        SearchExample(
            label = "Caf\u00e9s mit Terrasse",
            query = "Finde Cafes mit Terrasse",
            placeName = "Berlin Alexanderplatz",
            radiusM = "1000",
        ),
        SearchExample(
            label = "Sp\u00e4tis",
            query = "Finde Spaetis in der Naehe",
            placeName = "Berlin Kreuzberg",
            radiusM = "1200",
        ),
        SearchExample(
            label = "Spielpl\u00e4tze",
            query = "Finde Spielplaetze mit Wasserspielen",
            placeName = "Berlin Mitte",
            radiusM = "1500",
        ),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Schnellbeispiele",
            style = MaterialTheme.typography.titleSmall,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            examples.forEach { example ->
                OutlinedButton(
                    onClick = {
                        onExampleClick(
                            example.query,
                            example.placeName,
                            example.radiusM,
                        )
                    },
                ) {
                    Text(example.label)
                }
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator()

            Text(
                text = "OSMATE verarbeitet die Anfrage...",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Hinweis",
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    body: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
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
private suspend fun loadFreshCurrentLocation(context: Context): UserLocation? {
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
        locationManager.isProviderEnabled(provider)
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
private fun loadLastKnownLocation(locationManager: LocationManager): Location? {
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

private fun buildLocationStatus(location: UserLocation): String {
    val accuracyText = location.accuracyMeters?.let { accuracy ->
        "Genauigkeit: ${accuracy.toInt()} m"
    } ?: "Genauigkeit: unbekannt"

    val providerText = location.provider?.let { provider ->
        "Quelle: $provider"
    } ?: "Quelle: unbekannt"

    return "Aktuelle Position: ${location.latitude}, ${location.longitude}\n$accuracyText\n$providerText"
}

private data class SearchExample(
    val label: String,
    val query: String,
    val placeName: String,
    val radiusM: String,
)