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
    val hasCoordinates = latitude != null && longitude != null

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Ausgewählter Ort",
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )

            if (hasCoordinates) {
                Text(
                    text = "Lat: ${"%.5f".format(latitude)} | Lon: ${"%.5f".format(longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                )

                Button(
                    onClick = {
                        if (latitude != null && longitude != null) {
                            val uri = Uri.parse(
                                "https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude#map=18/$latitude/$longitude",
                            )

                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                uri,
                            )

                            context.startActivity(intent)
                        }
                    },
                ) {
                    Text("In OpenStreetMap öffnen")
                }
            } else {
                Text(
                    text = "Für dieses Objekt ist keine direkte Punktkoordinate verfügbar.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}