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
import ru.tusur.presentation.common.component.EditableDropdownSelector
import ru.tusur.presentation.common.component.CompactTextField
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryMetadataScreen(navController: NavController) {
    val viewModel: NewEntryMetadataViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
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

            // YEAR
            EditableDropdownSelector(
                label = "Select or add Year",
                items = uiState.years,
                selectedItem = uiState.selectedYear,
                itemToString = { it.value.toString() },
                onItemSelected = viewModel::onYearSelected,
                onAddNewItem = {
                    viewModel.onNewYearInputChanged(it)
                    viewModel.addNewYear()
                },
                onDeleteItem = viewModel::deleteYear,
                errorMessage = if (uiState.selectedYear == null) "Select or add a year" else null
            )

            Spacer(Modifier.height(12.dp))

            // BRAND
            EditableDropdownSelector(
                label = "Select or add Brand",
                items = uiState.brands,
                selectedItem = uiState.selectedBrand,
                itemToString = { it.name },
                onItemSelected = viewModel::onBrandSelected,
                onAddNewItem = {
                    viewModel.onNewBrandInputChanged(it)
                    viewModel.addNewBrand()
                },
                onDeleteItem = viewModel::deleteBrand,
                errorMessage = if (uiState.selectedBrand == null) "Select or add a brand" else null
            )

            Spacer(Modifier.height(12.dp))

            // MODEL
            EditableDropdownSelector(
                label = "Select or add Model",
                items = uiState.models,
                selectedItem = uiState.selectedModel,
                itemToString = { it.name },
                onItemSelected = viewModel::onModelSelected,
                onAddNewItem = {
                    viewModel.onNewModelInputChanged(it)
                    viewModel.addNewModel()
                },
                onDeleteItem = viewModel::deleteModel,
                errorMessage = if (uiState.selectedModel == null) "Select or add a model" else null
            )

            Spacer(Modifier.height(12.dp))

            // LOCATION
            EditableDropdownSelector(
                label = "Select or add Location",
                items = uiState.locations,
                selectedItem = uiState.selectedLocation,
                itemToString = { it.name },
                onItemSelected = viewModel::onLocationSelected,
                onAddNewItem = {
                    viewModel.onNewLocationInputChanged(it)
                    viewModel.addNewLocation()
                },
                onDeleteItem = viewModel::deleteLocation,
                errorMessage = if (uiState.selectedLocation == null) "Select or add a location" else null
            )

            Spacer(Modifier.height(16.dp))

            // TITLE
            CompactTextField(
                label = "Brief title (≤50)",
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                isError = uiState.title.isBlank(),
                errorMessage = if (uiState.title.isBlank()) "Title cannot be empty" else null
            )

            Spacer(Modifier.height(24.dp))

            // CONTINUE BUTTON
            Button(
                onClick = {
                    val id = uiState.entryId ?: 0L
                    val year = uiState.selectedYear!!.value
                    val brand = Uri.encode(uiState.selectedBrand!!.name)
                    val model = Uri.encode(uiState.selectedModel!!.name)
                    val location = Uri.encode(uiState.selectedLocation!!.name)
                    val title = Uri.encode(uiState.title)

                    navController.navigate(
                        "edit_entry/$id/$year/$brand/$model/$location/$title/description"
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
