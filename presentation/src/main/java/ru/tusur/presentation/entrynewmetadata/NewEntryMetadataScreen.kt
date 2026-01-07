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
                errorMessage = if (uiState.selectedYear == null)
                    stringResource(R.string.error_select_year)
                else null
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
                errorMessage = if (uiState.selectedBrand == null)
                    stringResource(R.string.error_select_brand)
                else null
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
                errorMessage = if (uiState.selectedModel == null)
                    stringResource(R.string.error_select_model)
                else null
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
                errorMessage = if (uiState.selectedLocation == null)
                    stringResource(R.string.error_select_location)
                else null
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
                Text(stringResource(R.string.button_continue))
            }
        }
    }
}
