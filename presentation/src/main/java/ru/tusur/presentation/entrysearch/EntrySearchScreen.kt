package ru.tusur.presentation.entrysearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.tusur.domain.model.SearchQuery
import ru.tusur.presentation.search.SharedSearchViewModel
import ru.tusur.domain.model.toFilter
import ru.tusur.presentation.R
import ru.tusur.presentation.common.component.EditableDropdownSelector

@Composable
fun EntrySearchScreen(
    navController: NavController,
    viewModel: EntrySearchViewModel = koinViewModel(),
    onSearch: (SearchQuery) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val sharedSearchViewModel: SharedSearchViewModel = koinViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // YEAR
        EditableDropdownSelector(
            items = uiState.years,
            selectedItem = uiState.selectedYear,
            itemToString = { it.toString() },
            onItemSelected = { viewModel.onYearSelected(it) },
            onAddNewItem = {},          // Search screen does NOT add new items
            onDeleteItem = null,        // Search screen does NOT delete items
            errorMessage = null         // No validation in search screen
        )

        Spacer(Modifier.height(12.dp))

        // BRAND
        EditableDropdownSelector(
            items = uiState.brands,
            selectedItem = uiState.selectedBrand,
            itemToString = { it.toString() },
            onItemSelected = { viewModel.onBrandSelected(it) },
            onAddNewItem = {},
            onDeleteItem = null,
            errorMessage = null
        )

        Spacer(Modifier.height(12.dp))

        // MODEL
        EditableDropdownSelector(
            items = uiState.models,
            selectedItem = uiState.selectedModel,
            itemToString = { it.toString() },
            onItemSelected = { viewModel.onModelSelected(it) },
            onAddNewItem = {},
            onDeleteItem = null,
            errorMessage = null
        )

        Spacer(Modifier.height(12.dp))

        // LOCATION
        EditableDropdownSelector(
            items = uiState.locations,
            selectedItem = uiState.selectedLocation,
            itemToString = { it.toString() },
            onItemSelected = { viewModel.onLocationSelected(it) },
            onAddNewItem = {},
            onDeleteItem = null,
            errorMessage = null
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val query = viewModel.buildSearchQuery()
                sharedSearchViewModel.updateFilter(query.toFilter())
                navController.navigate("search_entries") { launchSingleTop = true }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.main_search_entries))
        }
    }
}
