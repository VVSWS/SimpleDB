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

        // YEAR DROPDOWN
        YearDropdown(
            selected = uiState.entry.year,
            onSelected = viewModel::onYearChanged
        )

        // MODEL DROPDOWN
        ModelDropdown(
            selected = uiState.entry.model,
            onSelected = viewModel::onModelChanged
        )

        // LOCATION DROPDOWN
        LocationDropdown(
            selected = uiState.entry.location,
            onSelected = viewModel::onLocationChanged
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
