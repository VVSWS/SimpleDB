package ru.tusur.presentation.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
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
import ru.tusur.presentation.mainscreen.MainViewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()
    val mainUiState by mainViewModel.uiState.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }


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

    // SETTINGS EVENTS
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.DatabaseCreated -> {}
                is SettingsEvent.DatabaseError -> {}
            }
        }
    }

    // RESET SUCCESS TOAST
    LaunchedEffect(mainUiState.resetCompleted) {
        if (mainUiState.resetCompleted) {
            Toast.makeText(
                context,
                context.getString(R.string.settings_db_reset_success),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },

                actions = {
                    // Help icon
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = stringResource(R.string.cd_help)
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
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

            // MERGE DB
            Button(
                onClick = { mergeFolderLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.mergeProgress == null && uiState.exportProgress == null
            ) {
                Text(stringResource(R.string.settings_db_merge))
            }

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

            Spacer(modifier = Modifier.height(24.dp))

            // RESET DATABASE BUTTON + CONFIRMATION DIALOG
            var showResetDialog by remember { mutableStateOf(false) }

            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                enabled = uiState.mergeProgress == null && uiState.exportProgress == null
            ) {
                Text(stringResource(R.string.settings_db_reset))
            }

            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = {
                        Text(stringResource(R.string.settings_db_reset_title))
                    },
                    text = {
                        Text(stringResource(R.string.settings_db_reset_confirm))
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showResetDialog = false
                                mainViewModel.resetDatabase()
                            }
                        ) {
                            Text(stringResource(R.string.settings_reset))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
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

    // AlertDialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(stringResource(R.string.help_title_settings))
            },
            text = {
                Text(stringResource(R.string.help_information_settings))
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(stringResource(R.string.help_dialog_close_settings))
                }
            }
        )
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
