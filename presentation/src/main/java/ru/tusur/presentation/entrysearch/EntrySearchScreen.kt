package ru.tusur.presentation.entrysearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import ru.tusur.presentation.R
import ru.tusur.presentation.common.component.MetadataDropdown
import androidx.compose.ui.Alignment

@Composable
fun EntrySearchScreen(navController: NavController) {
    val viewModel: EntrySearchViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {
        // --- Кастомный Top Bar ---
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
                    text = stringResource(R.string.main_search_entries),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
        }

        // --- Основной контент ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Year
            MetadataDropdown(
                label = "Year",
                items = uiState.years,
                selectedItem = uiState.selectedYear,
                onItemSelected = { viewModel.onYearSelected(it) },
                itemToString = { it.value.toString() }
            )

            // Model
            MetadataDropdown(
                label = "Model",
                items = uiState.models,
                selectedItem = uiState.selectedModel,
                onItemSelected = { viewModel.onModelSelected(it) },
                itemToString = { it.name }
            )

            // Location
            MetadataDropdown(
                label = "Location",
                items = uiState.locations,
                selectedItem = uiState.selectedLocation,
                onItemSelected = { viewModel.onLocationSelected(it) },
                itemToString = { it.name }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val filter = viewModel.buildFilter()
                    // TODO: передать filter в EntryListScreen
                    navController.navigate("search_entries")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Find Entries")
            }
        }
    }
}