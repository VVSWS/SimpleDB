package ru.tusur.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import org.koin.androidx.compose.koinViewModel
import ru.tusur.core.util.FileHelper
import ru.tusur.presentation.R
import java.io.File

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()

    // MERGE: pick external DB file
    val pickDbForMerge = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = FileHelper.copyUriToTempFile(context, it)
            viewModel.mergeDatabase(file)
        }
    }

    // EXPORT: choose destination file
    val exportDbLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportDatabase(it) }
    }

    // EVENTS
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.LanguageChanged -> Unit
                is SettingsEvent.DatabaseError -> println("DB error: ${event.message}")
                is SettingsEvent.DatabaseCreated -> println("Database created")
                is SettingsEvent.DatabaseOpened -> println("Database opened")
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
                        contentDescription = "Back"
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

        // CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // LANGUAGE
            Text(
                text = stringResource(R.string.settings_language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LanguageRadioButton(
                label = stringResource(R.string.settings_language_en),
                selected = uiState.language == SettingsViewModel.Language.EN,
                onClick = { viewModel.setLanguage(SettingsViewModel.Language.EN) }
            )

            LanguageRadioButton(
                label = stringResource(R.string.settings_language_es),
                selected = uiState.language == SettingsViewModel.Language.ES,
                onClick = { viewModel.setLanguage(SettingsViewModel.Language.ES) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // THEME
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ThemeRadioButton(
                label = stringResource(R.string.settings_theme_system),
                selected = uiState.theme == SettingsViewModel.Theme.SYSTEM,
                onClick = { viewModel.setTheme(SettingsViewModel.Theme.SYSTEM) }
            )

            ThemeRadioButton(
                label = stringResource(R.string.settings_theme_light),
                selected = uiState.theme == SettingsViewModel.Theme.LIGHT,
                onClick = { viewModel.setTheme(SettingsViewModel.Theme.LIGHT) }
            )

            ThemeRadioButton(
                label = stringResource(R.string.settings_theme_dark),
                selected = uiState.theme == SettingsViewModel.Theme.DARK,
                onClick = { viewModel.setTheme(SettingsViewModel.Theme.DARK) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // DATABASE SECTION
            Text(
                text = stringResource(R.string.settings_db),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // CREATE NEW DB
            Button(
                onClick = { viewModel.createNewDatabase() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create New Database")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // MERGE DB
            Button(
                onClick = { pickDbForMerge.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Merge Database")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // EXPORT DB
            Button(
                onClick = { exportDbLauncher.launch("carfault_export.db") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Database")
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
private fun LanguageRadioButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
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
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}
