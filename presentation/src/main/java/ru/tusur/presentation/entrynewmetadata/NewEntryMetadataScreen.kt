package ru.tusur.presentation.entrynewmetadata

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import ru.tusur.presentation.common.component.EditableDropdownSelector
import ru.tusur.presentation.common.component.CompactTextField
import android.net.Uri
import ru.tusur.presentation.R
import ru.tusur.domain.model.FaultEntry


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryMetadataScreen(navController: NavController) {
    val viewModel: NewEntryMetadataViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.new_entry_metadata_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
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
                .imePadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // YEAR
            EditableDropdownSelector(
                items = uiState.years,
                selectedItem = uiState.selectedYear,
                itemToString = { it.value.toString() },
                onItemSelected = viewModel::onYearSelected,
                onAddNewItem = {
                    viewModel.onNewYearInputChanged(it)
                    viewModel.addNewYear()
                },
                onDeleteItem = viewModel::deleteYear,
                showAddNewOption = true,
                placeholder = stringResource(R.string.hint_select_or_add_year)
            )

            Spacer(Modifier.height(12.dp))

            // BRAND
            EditableDropdownSelector(
                items = uiState.brands,
                selectedItem = uiState.selectedBrand,
                itemToString = { it.name },
                onItemSelected = viewModel::onBrandSelected,
                onAddNewItem = {
                    viewModel.onNewBrandInputChanged(it)
                    viewModel.addNewBrand()
                },
                onDeleteItem = viewModel::deleteBrand,
                showAddNewOption = true,
                placeholder = stringResource(R.string.hint_select_or_add_brand)
            )

            Spacer(Modifier.height(12.dp))

            // MODEL
            EditableDropdownSelector(
                items = uiState.models,
                selectedItem = uiState.selectedModel,
                itemToString = { it.name },
                onItemSelected = viewModel::onModelSelected,
                onAddNewItem = {
                    viewModel.onNewModelInputChanged(it)
                    viewModel.addNewModel()
                },
                onDeleteItem = viewModel::deleteModel,
                showAddNewOption = true,
                placeholder = stringResource(R.string.hint_select_or_add_model)
            )

            Spacer(Modifier.height(12.dp))

            // LOCATION
            EditableDropdownSelector(
                items = uiState.locations,
                selectedItem = uiState.selectedLocation,
                itemToString = { it.name },
                onItemSelected = viewModel::onLocationSelected,
                onAddNewItem = {
                    viewModel.onNewLocationInputChanged(it)
                    viewModel.addNewLocation()
                },
                onDeleteItem = viewModel::deleteLocation,
                showAddNewOption = true,
                placeholder = stringResource(R.string.hint_select_or_add_location)
            )

            Spacer(Modifier.height(16.dp))

            // TITLE
            CompactTextField(
                label = stringResource(R.string.label_title_brief),
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                isError = uiState.title.isBlank(),
                errorMessage = if (uiState.title.isBlank())
                    stringResource(R.string.error_title_empty)
                else null
            )

            Spacer(Modifier.height(24.dp))

            // CONTINUE BUTTON
            Button(
                onClick = {
                    // Build the entry from UI state
                    val entry = FaultEntry(
                        year = uiState.selectedYear,              // ← pass Year? directly
                        brand = uiState.selectedBrand,           // if brand is also a domain model
                        model = uiState.selectedModel,           // same here
                        location = uiState.selectedLocation,     // and here
                        title = uiState.title,
                        timestamp = System.currentTimeMillis()
                    )
                    // Create entry and navigate
                    viewModel.createEntry { newId ->
                        navController.navigate("edit_entry_description/$newId")
                    }
                },
                enabled = uiState.isContinueEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_continue))
            }

        }
    }
}
