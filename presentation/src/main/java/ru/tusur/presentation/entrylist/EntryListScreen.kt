package ru.tusur.presentation.entrylist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import ru.tusur.stop.core.ui.component.FaultCard
import ru.tusur.stop.presentation.R
import java.time.LocalDateTime
import java.time.ZoneOffset

@Composable
fun EntryListScreen(
    navController: NavController,
    filter: String // "recent" or "search"
) {
    val viewModel: EntryListViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    // Загружаем при появлении экрана
    LaunchedEffect(filter) {
        val parsedFilter = when (filter) {
            "recent" -> EntryListViewModel.Filter.Recent
            else -> EntryListViewModel.Filter.Custom() // TODO: передавать параметры
        }
        viewModel.loadEntries(parsedFilter)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.main_recent_entries)) })
        },
        content = { padding ->
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.error}")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(uiState.entries) { entry ->
                            FaultCard(
                                model = entry.model.name,
                                year = entry.year.value,
                                location = entry.location.name,
                                title = entry.title,
                                timestamp = LocalDateTime.ofEpochSecond(
                                    entry.timestamp,
                                    0,
                                    ZoneOffset.UTC
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        navController.navigate("edit_entry/${entry.id}")
                                    }
                            )
                        }
                        if (uiState.entries.isEmpty()) {
                            item {
                                Text(
                                    text = "No entries found",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}