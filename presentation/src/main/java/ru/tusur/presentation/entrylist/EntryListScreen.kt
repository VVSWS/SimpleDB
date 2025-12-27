package ru.tusur.presentation.entrylist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.tusur.presentation.search.SharedSearchViewModel
import ru.tusur.domain.model.FaultEntry

@Composable
fun EntryListScreen(
    navController: NavController,
    isSearchMode: Boolean = false,
    viewModel: EntryListViewModel = koinViewModel(),
    sharedSearchViewModel: SharedSearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filter by sharedSearchViewModel.filter.collectAsState()
    println("DEBUG: isSearchMode=$isSearchMode, filter=$filter")


    // Load data depending on mode
    LaunchedEffect(isSearchMode, filter) {
        if (isSearchMode) {
            viewModel.searchEntries(filter)
        } else {
            viewModel.loadRecentEntries()
        }
    }

    val title = if (isSearchMode) "Search Results" else "Recent Entries"

    Column(modifier = Modifier.fillMaxSize()) {

        // Top bar
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // Loading indicator
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
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
            println("DEBUG: entries size = ${uiState.entries.size}, isSearchMode=$isSearchMode")

            items(uiState.entries) { entry ->
                EntryListItem(
                    entry = entry,
                    onClick = {
                        navController.navigate("view_entry/${entry.id}")
                    },
                    onDelete = {
                        viewModel.deleteEntry(entry)
                    }
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
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = entry.title ?: "(No title)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = entry.timestamp.toString(), style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        }
    }
}
