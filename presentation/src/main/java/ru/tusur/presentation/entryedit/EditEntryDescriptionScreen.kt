package ru.tusur.presentation.entryedit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import androidx.compose.ui.Alignment
import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.Year
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Location

@Composable
fun EditEntryDescriptionScreen(
    navController: NavController,
    entryId: Long?,
    year: Int,
    brand: String,
    model: String,
    location: String,
    title: String
) {
    val viewModel: EditEntryViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    // Apply metadata ONLY for new entries
    LaunchedEffect(Unit) {
        if (entryId == null || entryId == 0L) {
            viewModel.onYearChanged(Year(year))
            viewModel.onBrandChanged(Brand(brand))
            viewModel.onModelChanged(Model(model))
            viewModel.onLocationChanged(Location(location))
            viewModel.onTitleChanged(title)
        }
    }

    LaunchedEffect(entryId) {
        if (entryId != null && entryId > 0L) {
            viewModel.loadEntry(entryId)
        }
    }

    // Handle save completion
    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {
            viewModel.consumeSaveCompleted()
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Description",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = uiState.entry.description,
            onValueChange = viewModel::onDescriptionChanged,
            label = { Text("Description (≤500)") },
            maxLines = 6,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = viewModel::saveEntry,
                enabled = uiState.isValid && !uiState.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }

        if (uiState.isEditMode) {
            TextButton(
                onClick = viewModel::deleteEntry,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Delete Entry", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

