package com.osmate.app.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.osmate.app.data.model.SearchResultItem

@Composable
fun SelectedResultDetail(
    item: SearchResultItem?,
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

            CoordinateSection(
                item = item,
                onOpenInOpenStreetMap = { latitude, longitude ->
                    val url = createOpenStreetMapUrl(
                        latitude = latitude,
                        longitude = longitude,
                    )

                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url),
                    )

                    context.startActivity(intent)
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

private fun createOpenStreetMapUrl(
    latitude: Double,
    longitude: Double,
): String {
    return "https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude#map=18/$latitude/$longitude"
}

private fun formatCoordinate(value: Double): String {
    return "%.5f".format(value)
}