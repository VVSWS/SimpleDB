package ru.tusur.presentation.entrylist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import ru.tusur.core.ui.component.FaultCard
import ru.tusur.presentation.R
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
                    text = stringResource(R.string.main_recent_entries),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
        }

        // --- Основной контент ---
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${uiState.error}")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
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
                                .padding(4.dp)
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}