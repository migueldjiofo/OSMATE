package com.osmate.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.osmate.app.data.model.SearchResultItem

@Composable
fun SearchResultList(
    resultCount: Int,
    items: List<SearchResultItem>,
    selectedTitle: String,
    showEmptyState: Boolean,
    onItemClick: (SearchResultItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty() && !showEmptyState) {
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Gefundene Orte",
            style = MaterialTheme.typography.titleMedium,
        )

        ResultSummaryCard(
            resultCount = resultCount,
            visibleCount = items.take(10).size,
        )

        if (items.isEmpty()) {
            EmptyResultCard()
            return
        }

        items.take(10).forEachIndexed { index, item ->
            SearchResultCard(
                index = index + 1,
                item = item,
                isSelected = item.title == selectedTitle,
                onClick = {
                    onItemClick(item)
                },
            )
        }

        if (items.size > 10) {
            Text(
                text = "${items.size - 10} weitere Ergebnisse werden nicht angezeigt.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ResultSummaryCard(
    resultCount: Int,
    visibleCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "$resultCount Ergebnisse gefunden",
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                text = "$visibleCount Ergebnisse werden in der Liste angezeigt.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun EmptyResultCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Keine passenden Orte gefunden.",
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                text = "Passe die Suchanfrage, den Ort oder den Radius an.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    index: Int,
    item: SearchResultItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = item.latitude != null && item.longitude != null,
                onClick = onClick,
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (isSelected) {
                        "\u2713"
                    } else {
                        "$index."
                    },
                    style = MaterialTheme.typography.titleSmall,
                )

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                )
            }

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

            if (item.latitude != null && item.longitude != null) {
                Text(
                    text = "Lat: ${"%.5f".format(item.latitude)} | Lon: ${"%.5f".format(item.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Text(
                    text = "Keine Punktkoordinate verf\u00fcgbar",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}