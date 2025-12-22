package ru.tusur.presentation.entrynewmetadata

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import ru.tusur.presentation.R
import ru.tusur.presentation.common.component.EditableDropdown

@Composable
fun NewEntryMetadataScreen(navController: NavController) {
    val viewModel: NewEntryMetadataViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {

        // Top bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "New Entry Metadata",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // YEAR
            EditableDropdown(
                label = "Year",
                items = uiState.years,
                selectedItem = uiState.selectedYear,
                itemToString = { it.value.toString() },
                onItemSelected = { viewModel.onYearSelected(it) },
                onAddNewItem = {
                    viewModel.onNewYearInputChanged(it)
                    viewModel.addNewYear()
                },
                errorMessage = if (uiState.selectedYear == null) "Select or add a year" else null
            )

            Spacer(Modifier.height(16.dp))

            // MODEL
            EditableDropdown(
                label = "Model",
                items = uiState.models,
                selectedItem = uiState.selectedModel,
                itemToString = { it.name },
                onItemSelected = { viewModel.onModelSelected(it) },
                onAddNewItem = {
                    viewModel.onNewModelInputChanged(it)
                    viewModel.addNewModel()
                },
                errorMessage = if (uiState.selectedModel == null) "Select or add a model" else null
            )

            Spacer(Modifier.height(16.dp))

            // LOCATION
            EditableDropdown(
                label = "Location",
                items = uiState.locations,
                selectedItem = uiState.selectedLocation,
                itemToString = { it.name },
                onItemSelected = { viewModel.onLocationSelected(it) },
                onAddNewItem = {
                    viewModel.onNewLocationInputChanged(it)
                    viewModel.addNewLocation()
                },
                errorMessage = if (uiState.selectedLocation == null) "Select or add a location" else null
            )

            Spacer(Modifier.height(16.dp))

            // TITLE
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("Brief title (≤50)") },
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.title.isBlank()
            )

            if (uiState.title.isBlank()) {
                Text(
                    text = "Title cannot be empty",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("edit_entry") },
                enabled = uiState.isContinueEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}
