package ru.tusur.presentation.entrysearch

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
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import ru.tusur.presentation.common.component.EditableDropdown
import ru.tusur.presentation.search.SharedSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrySearchScreen(navController: NavController) {
    val viewModel: EntrySearchViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val sharedSearchViewModel: SharedSearchViewModel = koinViewModel()


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Search Entries",
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
            EditableDropdown(
                label = "Year",
                items = uiState.years,
                selectedItem = uiState.selectedYear,
                itemToString = { it.value.toString() },
                onItemSelected = { viewModel.onYearSelected(it) },
                onAddNewItem = {}, // Search screen does NOT add new items
                errorMessage = null
            )

            Spacer(Modifier.height(16.dp))

            // MODEL
            EditableDropdown(
                label = "Model",
                items = uiState.models,
                selectedItem = uiState.selectedModel,
                itemToString = { it.name },
                onItemSelected = { viewModel.onModelSelected(it) },
                onAddNewItem = {},
                errorMessage = null
            )

            Spacer(Modifier.height(16.dp))

            // LOCATION
            EditableDropdown(
                label = "Location",
                items = uiState.locations,
                selectedItem = uiState.selectedLocation,
                itemToString = { it.name },
                onItemSelected = { viewModel.onLocationSelected(it) },
                onAddNewItem = {},
                errorMessage = null
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val filter = viewModel.buildFilter()
                    sharedSearchViewModel.setFilter(filter)
                    navController.navigate("search_entries")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Find Entries")
            }
        }
    }
}
