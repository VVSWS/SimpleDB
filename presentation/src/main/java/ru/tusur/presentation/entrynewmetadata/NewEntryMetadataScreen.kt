package ru.tusur.presentation.entrynewmetadata

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import ru.tusur.presentation.common.component.EditableDropdown
import android.net.Uri


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryMetadataScreen(navController: NavController) {
    val viewModel: NewEntryMetadataViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val entrySavedFlow = savedStateHandle?.getStateFlow("entry_saved", false)

    LaunchedEffect(entrySavedFlow?.value) {
        if (entrySavedFlow?.value == true) {
            snackbarHostState.showSnackbar("Entry saved successfully")
            savedStateHandle?.set("entry_saved", false)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "New Entry Metadata",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

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
                onClick = {
                    val safeId = uiState.entryId ?: 0L

                    val encodedModel = Uri.encode(uiState.selectedModel!!.name)
                    val encodedLocation = Uri.encode(uiState.selectedLocation!!.name)
                    val encodedTitle = Uri.encode(uiState.title)

                    navController.navigate(
                        "edit_entry/" +
                                "$safeId/" +
                                "${uiState.selectedYear!!.value}/" +
                                "$encodedModel/" +
                                "$encodedLocation/" +
                                "$encodedTitle/description"
                    )
                },
                enabled = uiState.isContinueEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }

        }
    }
}
