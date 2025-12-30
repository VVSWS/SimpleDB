package ru.tusur.presentation.entrysearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.tusur.domain.model.SearchQuery
import ru.tusur.presentation.search.SharedSearchViewModel
import ru.tusur.domain.model.toFilter


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
        DropdownSelector(
            label = "Year",
            items = uiState.years,
            selected = uiState.selectedYear,
            onSelected = { viewModel.onYearSelected(it) }
        )

        Spacer(Modifier.height(12.dp))

        // BRAND
        DropdownSelector(
            label = "Brand",
            items = uiState.brands,
            selected = uiState.selectedBrand,
            onSelected = { viewModel.onBrandSelected(it) }
        )

        Spacer(Modifier.height(12.dp))

        // MODEL (filtered)
        DropdownSelector(
            label = "Model",
            items = uiState.models,
            selected = uiState.selectedModel,
            onSelected = { viewModel.onModelSelected(it) }
        )

        Spacer(Modifier.height(12.dp))

        // LOCATION
        DropdownSelector(
            label = "Location",
            items = uiState.locations,
            selected = uiState.selectedLocation,
            onSelected = { viewModel.onLocationSelected(it) }
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
            Text("Search")
        }
    }
}

@Composable
private fun <T> DropdownSelector(
    label: String,
    items: List<T>,
    selected: T?,
    onSelected: (T?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selected?.toString() ?: "Select…")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Any") },
                    onClick = {
                        onSelected(null)
                        expanded = false
                    }
                )

                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.toString()) },
                        onClick = {
                            onSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
