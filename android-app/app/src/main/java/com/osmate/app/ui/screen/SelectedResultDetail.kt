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
    val latitude = item.latitude
    val longitude = item.longitude

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

            if (latitude != null && longitude != null) {
                val openStreetMapUrl = "https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude#map=18/$latitude/$longitude"

                Text(
                    text = "Lat: ${"%.5f".format(latitude)} | Lon: ${"%.5f".format(longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                )

                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(openStreetMapUrl),
                        )

                        context.startActivity(intent)
                    },
                ) {
                    Text("In OpenStreetMap \u00f6ffnen")
                }
            } else {
                Text(
                    text = "F\u00fcr dieses Objekt ist keine direkte Punktkoordinate verf\u00fcgbar.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}