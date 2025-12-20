package ru.tusur.presentation.entrysearch

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import ru.tusur.stop.presentation.R
import ru.tusur.stop.presentation.common.component.MetadataDropdown

@Composable
fun EntrySearchScreen(navController: NavController) {
    val viewModel: EntrySearchViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.main_search_entries)) })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Year
                MetadataDropdown(
                    label = "Year",
                    items = uiState.years,
                    selectedItem = uiState.selectedYear,
                    onItemSelected = { viewModel.onYearSelected(it) },
                    itemToString = { it.value.toString() }
                )

                // Model
                MetadataDropdown(
                    label = "Model",
                    items = uiState.models,
                    selectedItem = uiState.selectedModel,
                    onItemSelected = { viewModel.onModelSelected(it) },
                    itemToString = { it.name }
                )

                // Location
                MetadataDropdown(
                    label = "Location",
                    items = uiState.locations,
                    selectedItem = uiState.selectedLocation,
                    onItemSelected = { viewModel.onLocationSelected(it) },
                    itemToString = { it.name }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val filter = viewModel.buildFilter()
                        // TODO: передать filter в EntryListScreen
                        navController.navigate("search_entries")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_create_db))
                }
            }
        }
    )
}