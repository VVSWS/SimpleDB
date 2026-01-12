package ru.tusur.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import ru.tusur.presentation.R
import ru.tusur.core.ui.theme.ThemeMode





@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()

    // MERGE FOLDER PICKER
    val mergeFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { viewModel.mergeDatabase(it) }
    }

    // EXPORT FOLDER PICKER
    val exportFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { viewModel.exportDatabaseToFolder(it) }
    }

    // EVENTS
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.DatabaseCreated -> {}
                is SettingsEvent.DatabaseError -> {}
                is SettingsEvent.DatabaseExists -> {}
                is SettingsEvent.DatabaseOpened -> {}
            }
        }
    }

    Column(Modifier.fillMaxSize()) {

        // TOP BAR
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back)
                    )
                }

                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
        }

        // MAIN CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // THEME SECTION
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ThemeRadioButton(
                label = stringResource(R.string.settings_theme_system),
                selected = uiState.theme == ThemeMode.SYSTEM,
                onClick = { viewModel.setTheme(ThemeMode.SYSTEM) }
            )

            ThemeRadioButton(
                label = stringResource(R.string.settings_theme_light),
                selected = uiState.theme == ThemeMode.LIGHT,
                onClick = { viewModel.setTheme(ThemeMode.LIGHT) }
            )

            ThemeRadioButton(
                label = stringResource(R.string.settings_theme_dark),
                selected = uiState.theme == ThemeMode.DARK,
                onClick = { viewModel.setTheme(ThemeMode.DARK) }
            )


            Spacer(modifier = Modifier.height(24.dp))

            // DATABASE SECTION
            Text(
                text = stringResource(R.string.settings_db),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // CREATE DB
            Button(
                onClick = { viewModel.createNewDatabase() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.mergeProgress == null && uiState.exportProgress == null
            ) {
                Text(stringResource(R.string.settings_db_create))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // MERGE DB
            Button(
                onClick = { mergeFolderLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.mergeProgress == null && uiState.exportProgress == null
            ) {
                Text(stringResource(R.string.settings_db_merge))
            }

            // MERGE PROGRESS BAR
            uiState.mergeProgress?.let { progress ->
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // EXPORT DB
            Button(
                onClick = { exportFolderLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.mergeProgress == null && uiState.exportProgress == null
            ) {
                Text(stringResource(R.string.settings_db_export))
            }

            // EXPORT PROGRESS BAR
            uiState.exportProgress?.let { progress ->
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DELETE DATABASE BUTTON
            var showDeleteDialog by remember { mutableStateOf(false) }

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                enabled = uiState.mergeProgress == null && uiState.exportProgress == null
            ) {
                Text(stringResource(R.string.settings_db_delete))
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = {
                        Text(stringResource(R.string.settings_db_delete_title))
                    },
                    text = {
                        Text(stringResource(R.string.settings_db_delete_confirm))
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                viewModel.deleteDatabase()
                            }
                        ) {
                            Text(stringResource(R.string.settings_delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(R.string.settings_cancel))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            // MESSAGE
            uiState.message?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun ThemeRadioButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
