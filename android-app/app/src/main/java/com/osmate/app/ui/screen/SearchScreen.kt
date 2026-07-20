package com.osmate.app.ui.screen

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.osmate.app.ui.map.OsmateMapView
import com.osmate.app.ui.state.SearchUiState

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onPlaceNameChange: (String) -> Unit,
    onRadiusChange: (String) -> Unit,
    onCheckBackendClick: () -> Unit,
    onCreatePlanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                Text("Plan erstellen")
            }
        }

        OsmateMapView(
            geoJson = state.geoJson,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
        )

        if (state.isLoading) {
            CircularProgressIndicator()
        }

        SearchResultList(
            items = state.resultItems,
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
            InfoCard(
                title = "Fehler",
                body = state.errorMessage,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
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