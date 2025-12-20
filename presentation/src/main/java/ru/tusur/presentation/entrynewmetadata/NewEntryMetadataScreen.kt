package ru.tusur.presentation.entrynewmetadata

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun NewEntryMetadataScreen(navController: NavController) {
    val viewModel: NewEntryMetadataViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Entry Metadata") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Year
                MetadataDropdown(
                    label = "Year",
                    items = uiState.years,
                    selectedItem = uiState.selectedYear,
                    onItemSelected = { viewModel.onYearSelected(it) },
                    itemToString = { it.value.toString() },
                    onAddNewItem = { input ->
                        viewModel.onNewYearInputChanged(input)
                        viewModel.addNewYear()
                    }
                )

                // Model
                MetadataDropdown(
                    label = "Model",
                    items = uiState.models,
                    selectedItem = uiState.selectedModel,
                    onItemSelected = { viewModel.onModelSelected(it) },
                    itemToString = { it.name },
                    onAddNewItem = { input ->
                        viewModel.onNewModelInputChanged(input)
                        viewModel.addNewModel()
                    }
                )

                // Location
                MetadataDropdown(
                    label = "Location",
                    items = uiState.locations,
                    selectedItem = uiState.selectedLocation,
                    onItemSelected = { viewModel.onLocationSelected(it) },
                    itemToString = { it.name },
                    onAddNewItem = { input ->
                        viewModel.onNewLocationInputChanged(input)
                        viewModel.addNewLocation()
                    }
                )

                // Title
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChanged(it) },
                    label = { Text("Brief title (≤50)") },
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // TODO: передать метаданные в EditEntryScreen
                        navController.navigate("edit_entry")
                    },
                    enabled = uiState.isContinueEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue")
                }
            }
        }
    )
}