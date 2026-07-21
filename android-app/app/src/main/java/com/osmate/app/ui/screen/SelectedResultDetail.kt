package com.osmate.app.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.osmate.app.data.model.SearchResultItem
import com.osmate.app.domain.navigation.RouteEstimate
import com.osmate.app.domain.navigation.RouteEstimator
import com.osmate.app.domain.navigation.TravelMode

@Composable
fun SelectedResultDetail(
    item: SearchResultItem?,
    userLatitude: Double?,
    userLongitude: Double?,
    selectedTravelMode: TravelMode,
    onTravelModeChange: (TravelMode) -> Unit,
    routeDistanceText: String,
    routeDurationText: String,
    routeProvider: String,
    routeWarnings: List<String>,
    isRouteLoading: Boolean,
    onCalculateRouteClick: (Double?, Double?, Double?, Double?, TravelMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (item == null) {
        return
    }

    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Ausgew\u00e4hlter Ort",
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                text = item.primaryTag,
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
            )

            if (item.openingHours.isNotBlank()) {
                Text(
                    text = "\u00d6ffnungszeiten: ${item.openingHours}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            PlaceMetadataSection(
                item = item,
            )

            CoordinateSection(
                item = item,
                onOpenInOpenStreetMap = { latitude, longitude ->
                    openUrl(
                        context = context,
                        url = createOpenStreetMapUrl(
                            latitude = latitude,
                            longitude = longitude,
                        ),
                    )
                },
            )

            NavigationSection(
                item = item,
                userLatitude = userLatitude,
                userLongitude = userLongitude,
                selectedTravelMode = selectedTravelMode,
                onTravelModeChange = onTravelModeChange,
                routeDistanceText = routeDistanceText,
                routeDurationText = routeDurationText,
                routeProvider = routeProvider,
                routeWarnings = routeWarnings,
                isRouteLoading = isRouteLoading,
                onCalculateRouteClick = onCalculateRouteClick,
                onOpenNavigation = { startLat, startLon, destinationLat, destinationLon, mode ->
                    openUrl(
                        context = context,
                        url = createNavigationUrl(
                            startLatitude = startLat,
                            startLongitude = startLon,
                            destinationLatitude = destinationLat,
                            destinationLongitude = destinationLon,
                            mode = mode,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun CoordinateSection(
    item: SearchResultItem,
    onOpenInOpenStreetMap: (Double, Double) -> Unit,
) {
    val latitude = item.latitude
    val longitude = item.longitude

    when {
        latitude == null || longitude == null -> {
            Text(
                text = "F\u00fcr dieses Objekt ist keine direkte Punktkoordinate verf\u00fcgbar.",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        else -> {
            Text(
                text = "Lat: ${formatCoordinate(latitude)} | Lon: ${formatCoordinate(longitude)}",
                style = MaterialTheme.typography.bodySmall,
            )

            Button(
                onClick = {
                    onOpenInOpenStreetMap(
                        latitude,
                        longitude,
                    )
                },
            ) {
                Text("In OpenStreetMap \u00f6ffnen")
            }
        }
    }
}

@Composable
private fun NavigationSection(
    item: SearchResultItem,
    userLatitude: Double?,
    userLongitude: Double?,
    selectedTravelMode: TravelMode,
    onTravelModeChange: (TravelMode) -> Unit,
    routeDistanceText: String,
    routeDurationText: String,
    routeProvider: String,
    routeWarnings: List<String>,
    isRouteLoading: Boolean,
    onCalculateRouteClick: (Double?, Double?, Double?, Double?, TravelMode) -> Unit,
    onOpenNavigation: (Double, Double, Double, Double, TravelMode) -> Unit,
) {
    val destinationLatitude = item.latitude
    val destinationLongitude = item.longitude

    Text(
        text = "Navigation",
        style = MaterialTheme.typography.titleSmall,
    )

    if (userLatitude == null || userLongitude == null) {
        Text(
            text = "Bitte zuerst \u201eMeine Position\u201c verwenden.",
            style = MaterialTheme.typography.bodySmall,
        )
        return
    }

    if (destinationLatitude == null || destinationLongitude == null) {
        Text(
            text = "F\u00fcr dieses Objekt kann keine Navigation berechnet werden.",
            style = MaterialTheme.typography.bodySmall,
        )
        return
    }

    TravelModeSelector(
        selectedTravelMode = selectedTravelMode,
        onTravelModeChange = onTravelModeChange,
    )

    val estimate = RouteEstimator.estimateDirectDistance(
        startLatitude = userLatitude,
        startLongitude = userLongitude,
        destinationLatitude = destinationLatitude,
        destinationLongitude = destinationLongitude,
        mode = selectedTravelMode,
    )

    DirectEstimateText(
        estimate = estimate,
    )

    Button(
        onClick = {
            onCalculateRouteClick(
                userLatitude,
                userLongitude,
                destinationLatitude,
                destinationLongitude,
                selectedTravelMode,
            )
        },
        enabled = !isRouteLoading,
    ) {
        Text("Route berechnen")
    }

    if (isRouteLoading) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CircularProgressIndicator()

            Text(
                text = "Route wird berechnet...",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    if (routeDistanceText.isNotBlank() && routeDurationText.isNotBlank()) {
        Text(
            text = "Backend-Route: $routeDistanceText | $routeDurationText",
            style = MaterialTheme.typography.bodySmall,
        )

        if (routeProvider.isNotBlank()) {
            Text(
                text = "Routing-Anbieter: $routeProvider",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    routeWarnings.forEach { warning ->
        Text(
            text = "Hinweis: $warning",
            style = MaterialTheme.typography.bodySmall,
        )
    }

    Button(
        onClick = {
            onOpenNavigation(
                userLatitude,
                userLongitude,
                destinationLatitude,
                destinationLongitude,
                selectedTravelMode,
            )
        },
    ) {
        Text("Navigation \u00f6ffnen")
    }
}

@Composable
private fun TravelModeSelector(
    selectedTravelMode: TravelMode,
    onTravelModeChange: (TravelMode) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TravelMode.entries.forEach { mode ->
            val isSelected = mode == selectedTravelMode

            if (isSelected) {
                Button(
                    onClick = {
                        onTravelModeChange(mode)
                    },
                ) {
                    Text(mode.label)
                }
            } else {
                OutlinedButton(
                    onClick = {
                        onTravelModeChange(mode)
                    },
                ) {
                    Text(mode.label)
                }
            }
        }
    }
}

@Composable
private fun DirectEstimateText(
    estimate: RouteEstimate,
) {
    Text(
        text = "Direkte Distanz: ${RouteEstimator.formatDistance(estimate.distanceMeters)}",
        style = MaterialTheme.typography.bodySmall,
    )

    Text(
        text = "Direkte Sch\u00e4tzung (${estimate.mode.label}): ${RouteEstimator.formatDuration(estimate.durationMinutes)}",
        style = MaterialTheme.typography.bodySmall,
    )
}

private fun createOpenStreetMapUrl(
    latitude: Double,
    longitude: Double,
): String {
    return "https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude#map=18/$latitude/$longitude"
}

private fun createNavigationUrl(
    startLatitude: Double,
    startLongitude: Double,
    destinationLatitude: Double,
    destinationLongitude: Double,
    mode: TravelMode,
): String {
    return "https://www.google.com/maps/dir/?api=1&origin=$startLatitude,$startLongitude&destination=$destinationLatitude,$destinationLongitude&travelmode=${mode.mapsTravelMode}"
}

private fun openUrl(
    context: Context,
    url: String,
) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(url),
    )

    context.startActivity(intent)
}

private fun formatCoordinate(value: Double): String {
    return "%.5f".format(value)
}
@Composable
private fun PlaceMetadataSection(
    item: SearchResultItem,
) {
    val details = listOf(
        "K\u00fcche" to item.cuisine,
        "Website" to item.website,
        "Telefon" to item.phone,
        "Marke" to item.brand,
        "Betreiber" to item.operator,
        "Terrasse" to item.outdoorSeating,
        "WLAN" to item.internetAccess,
        "Barrierefreiheit" to item.wheelchair,
        "Take-away" to item.takeaway,
    ).filter { detail ->
        detail.second.isNotBlank()
    }

    if (details.isEmpty()) {
        return
    }

    Text(
        text = "Details",
        style = MaterialTheme.typography.titleSmall,
    )

    details.forEach { detail ->
        Text(
            text = "${detail.first}: ${detail.second}",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
