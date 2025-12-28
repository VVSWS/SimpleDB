package ru.tusur.presentation.entryedit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import ru.tusur.domain.model.Year
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Location
import ru.tusur.presentation.common.component.EditableDropdown
import ru.tusur.presentation.entryedit.components.LocationDropdown
import ru.tusur.presentation.entryedit.components.ModelDropdown
import ru.tusur.presentation.entryedit.components.YearDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryMetadataScreen(
    navController: NavController,
    entryId: Long?
) {
    val viewModel: EditEntryViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    // Load entry when screen opens
    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = if (uiState.isEditMode) "Edit Entry" else "New Entry",
            style = MaterialTheme.typography.headlineSmall
        )

        // TITLE
        OutlinedTextField(
            value = uiState.entry.title,
            onValueChange = viewModel::onTitleChanged,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        // YEAR
        EditableDropdown(
            label = "Year",
            items = uiState.years,
            selectedItem = uiState.entry.year,
            itemToString = { it.value.toString() },
            onItemSelected = viewModel::onYearChanged,
            onAddNewItem = {
                viewModel.onNewYearInputChanged(it)
                viewModel.addNewYear()
            },
            errorMessage = null
        )

        // BRAND
        EditableDropdown(
            label = "Brand",
            items = uiState.brands,
            selectedItem = uiState.entry.brand,
            itemToString = { it.name },
            onItemSelected = viewModel::onBrandChanged,
            onAddNewItem = {
                viewModel.onNewBrandInputChanged(it)
                viewModel.addNewBrand()
            },
            errorMessage = null
        )

        // MODEL
        EditableDropdown(
            label = "Model",
            items = uiState.models,
            selectedItem = uiState.entry.model,
            itemToString = { it.name },
            onItemSelected = viewModel::onModelChanged,
            onAddNewItem = {
                viewModel.onNewModelInputChanged(it)
                viewModel.addNewModel()
            },
            errorMessage = null
        )

        // LOCATION
        EditableDropdown(
            label = "Location",
            items = uiState.locations,
            selectedItem = uiState.entry.location,
            itemToString = { it.name },
            onItemSelected = viewModel::onLocationChanged,
            onAddNewItem = {
                viewModel.onNewLocationInputChanged(it)
                viewModel.addNewLocation()
            },
            errorMessage = null
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("edit_entry/${uiState.entry.id}/description")
            },
            enabled = uiState.entry.title.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Next")
        }
    }
}

