package ru.tusur.presentation.entrylist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import ru.tusur.domain.model.FaultEntry
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.foundation.clickable
import androidx.compose.material3.HorizontalDivider




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryListScreen(
    navController: NavController,
    filter: String
) {
    val viewModel: EntryListViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(filter) {
        viewModel.loadEntries(filter)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (filter) {
                            "recent" -> "Recent Entries"
                            "search" -> "Search Results"
                            else -> "Entries"
                        },
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

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.entries.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No entries found")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(
                        items = uiState.entries,
                        key = { it.id ?: it.hashCode().toLong() }
                    ) { entry ->
                        SwipeToDeleteEntryItem(
                            entry = entry,
                            onClick = {
                                navController.navigate("edit_entry/${entry.id}/metadata")
                            },
                            onDelete = { viewModel.deleteEntry(entry) }
                        )
                        HorizontalDivider()

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteEntryItem(
    entry: FaultEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val state = rememberSwipeToDismissBoxState()

    // When the user finishes swiping, check the final state
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            // Reset state so the item doesn't stay dismissed
            state.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    android.util.Log.d("EntryList", "Entry clicked: id=${entry.id}, title=${entry.title}")
                    onClick()
                }

        )
    }
}
