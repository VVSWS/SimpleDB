package ru.tusur.presentation.entryedit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    navController: NavController,
    entryId: Long?
) {
    val viewModel: EditEntryViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    // Load entry on first appearance
    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
    }

    // Scroll behavior for Material3 TopAppBar
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Handle save result → send event to metadata screen → navigate back
    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {

            // Send result to the correct screen
            navController.getBackStackEntry("new_metadata")
                .savedStateHandle["entry_saved"] = true

            viewModel.consumeSaveCompleted()

            // Navigate back
            navController.popBackStack()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            EditEntryTopBar(
                isEditMode = uiState.isEditMode,
                onBack = { navController.popBackStack() },
                onDelete = { viewModel.deleteEntry() },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // DESCRIPTION FIELD
            OutlinedTextField(
                value = uiState.entry.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Description (≤500)") },
                maxLines = 6,
                isError = uiState.descriptionError != null,
                supportingText = {
                    uiState.descriptionError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = viewModel::saveEntry,
                    enabled = uiState.isValid && !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                        Spacer(Modifier.width(6.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryTopBar(
    isEditMode: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = if (isEditMode) "Edit Entry" else "New Entry",
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (isEditMode) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}
