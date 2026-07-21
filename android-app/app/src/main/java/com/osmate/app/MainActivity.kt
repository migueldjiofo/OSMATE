package com.osmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.osmate.app.ui.screen.SearchScreen
import com.osmate.app.ui.theme.OSMATETheme
import com.osmate.app.ui.viewmodel.SearchViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OSMATETheme {
                val viewModel: SearchViewModel = viewModel()
                val state by viewModel.uiState.collectAsState()

                SearchScreen(
                    state = state,
                    onQueryChange = viewModel::updateQuery,
                    onPlaceNameChange = viewModel::updatePlaceName,
                    onRadiusChange = viewModel::updateRadius,
                    onCheckBackendClick = viewModel::checkBackend,
                    onCreatePlanClick = viewModel::createPlan,
                    onResetClick = viewModel::resetResults,
                    onExampleClick = viewModel::applySearchExample,
                    onResultClick = viewModel::selectResult,
                    onCalculateRouteClick = viewModel::calculateRoute,
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
        }
    }
}