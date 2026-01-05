package ru.tusur.presentation.entrylist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.tusur.presentation.search.SharedSearchViewModel
import ru.tusur.domain.model.FaultEntry
import ru.tusur.presentation.common.ConfirmDeleteDialog

@Composable
fun EntryListScreen(
    navController: NavController,
    isSearchMode: Boolean = false,
    viewModel: EntryListViewModel = koinViewModel(),
    sharedSearchViewModel: SharedSearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filter by sharedSearchViewModel.filter.collectAsState()

    // Load data depending on mode
    LaunchedEffect(isSearchMode, filter) {
        if (isSearchMode) {
            viewModel.searchEntries(filter)
        } else {
            viewModel.loadRecentEntries()
        }
    }

    val title = if (isSearchMode) "Search Results" else "Recent Entries"

    var entryToDelete by remember { mutableStateOf<FaultEntry?>(null) }

    // Delete confirmation dialog
    if (entryToDelete != null) {
        ConfirmDeleteDialog(
            itemName = entryToDelete!!.title ?: "this entry",
            onConfirm = {
                viewModel.deleteEntry(entryToDelete!!)
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Top bar
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        // Error message
        uiState.error?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Entries list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.entries) { entry ->
                EntryListItem(
                    entry = entry,
                    onClick = { navController.navigate("view_entry/${entry.id}") },
                    onDelete = { entryToDelete = entry }
                )
            }
        }
    }
}

@Composable
fun EntryListItem(
    entry: FaultEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Left side: title + metadata
            Column(modifier = Modifier.weight(1f)) {

                // Build a compact metadata string
                val meta = listOfNotNull(
                    entry.year?.value?.toString(),
                    entry.brand?.name,
                    entry.model?.name,
                    entry.location?.name
                ).joinToString(" • ")

                Text(
                    text = entry.title ?: "(No title)",
                    style = MaterialTheme.typography.titleMedium
                )

                if (meta.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right side: delete icon
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete entry"
                )
            }
        }
    }
}
