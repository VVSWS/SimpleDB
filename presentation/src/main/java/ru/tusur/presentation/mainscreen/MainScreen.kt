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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.tusur.presentation.R

@Composable
fun MainScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    val viewModel: MainViewModel = koinViewModel(viewModelStoreOwner = backStackEntry)
    val uiState by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {

        // ---------------------------------------------------------
        // TOP BAR
        // ---------------------------------------------------------
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

        // ---------------------------------------------------------
        // MAIN CONTENT
        // ---------------------------------------------------------
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------------------------------------------------------
            // DATABASE INFO CARD
            // ---------------------------------------------------------
            item {
                DatabaseInfoCard(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ---------------------------------------------------------
            // ACTION BUTTONS
            // ---------------------------------------------------------
            item {
                Button(
                    onClick = { navController.navigate("recent_entries") },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.main_recent_entries))
                }
            }

            item {
                Button(
                    onClick = { navController.navigate("new_metadata") },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.main_add_entry))
                }
            }

            item {
                Button(
                    onClick = { navController.navigate("search") },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.main_search_entries))
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DatabaseInfoCard(uiState: MainUiState) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = stringResource(R.string.main_db_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = stringResource(R.string.main_db_name), value = uiState.dbName)
            InfoRow(label = stringResource(R.string.main_entry_count), value = uiState.entryCount.toString())
            InfoRow(label = stringResource(R.string.main_db_size), value = uiState.dbSizeBytes.toReadableSize())
            InfoRow(label = stringResource(R.string.main_image_count), value = uiState.imageCount.toString())
            InfoRow(label = stringResource(R.string.main_images_folder_size), value = uiState.imagesFolderSizeBytes.toReadableSize())
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun Long.toReadableSize(): String {
    if (this <= 0) return "0 B"

    return when {
        this < 1024 -> "< 1 KB"
        this < 1024 * 1024 -> "${this / 1024} KB"
        else -> String.format("%.2f MB", this / (1024f * 1024f))
    }
}

