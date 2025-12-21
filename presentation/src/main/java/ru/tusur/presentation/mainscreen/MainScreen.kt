package ru.tusur.presentation.mainscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import ru.tusur.core.ui.component.StatusBanner
import ru.tusur.presentation.R

@Composable
fun MainScreen(navController: NavController) {
    val viewModel: MainViewModel = koinInject()
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
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )

                IconButton(
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.main_settings)
                    )
                }

                IconButton(
                    onClick = { navController.navigate("about") },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.main_about)
                    )
                }
            }
        }

        // --- Основной контент ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Recent Entries button
            item {
                Button(
                    onClick = { navController.navigate("recent_entries") },
                    enabled = uiState.isActive,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.main_recent_entries))
                }
            }

            // Add Entry button
            item {
                Button(
                    onClick = { navController.navigate("new_metadata") },
                    enabled = uiState.isActive,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.main_add_entry))
                }
            }

            // Search Entries button
            item {
                Button(
                    onClick = { navController.navigate("search") },
                    enabled = uiState.isActive,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.main_search_entries))
                }
            }

            // Status bar
            item {
                Spacer(modifier = Modifier.height(32.dp))
                if (uiState.isActive) {
                    Text(
                        text = stringResource(
                            R.string.main_status_with_db,
                            uiState.filename ?: "current.db",
                            uiState.entryCount
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    StatusBanner(
                        text = stringResource(R.string.main_status_no_db),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}