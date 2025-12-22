package ru.tusur.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import ru.tusur.presentation.R

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = koinViewModel()

    // Launchers
    val openDbLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.handleDbSelected(it) }
    }

    val openFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                context.contentResolver.takePersistableUriPermission(it, takeFlags)
            }
        }
    }

    // Handle one-off events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.LanguageChanged -> {
                    // no-op for now, or hook into your localization system
                }

                is SettingsEvent.DatabaseError -> {
                    // You can show a Snackbar, Toast, or dialog here
                    // For now, just stay on screen.
                }

                SettingsEvent.DatabaseCreated,
                SettingsEvent.DatabaseOpened -> {
                    navController.popBackStack()
                }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
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
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.settings_language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LanguageRadioButton(
                label = stringResource(R.string.settings_language_en),
                selected = viewModel.uiState.language == SettingsViewModel.Language.EN,
                onClick = { viewModel.setLanguage(SettingsViewModel.Language.EN) }
            )

            LanguageRadioButton(
                label = stringResource(R.string.settings_language_es),
                selected = viewModel.uiState.language == SettingsViewModel.Language.ES,
                onClick = { viewModel.setLanguage(SettingsViewModel.Language.ES) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ThemeRadioButton(
                label = stringResource(R.string.settings_theme_system),
                selected = viewModel.uiState.theme == SettingsViewModel.Theme.SYSTEM,
                onClick = { viewModel.setTheme(SettingsViewModel.Theme.SYSTEM) }
            )

            ThemeRadioButton(
                label = stringResource(R.string.settings_theme_light),
                selected = viewModel.uiState.theme == SettingsViewModel.Theme.LIGHT,
                onClick = { viewModel.setTheme(SettingsViewModel.Theme.LIGHT) }
            )

            ThemeRadioButton(
                label = stringResource(R.string.settings_theme_dark),
                selected = viewModel.uiState.theme == SettingsViewModel.Theme.DARK,
                onClick = { viewModel.setTheme(SettingsViewModel.Theme.DARK) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_db),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Button(
                onClick = { viewModel.createNewDatabase() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_create_db))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { openDbLauncher.launch(arrayOf("application/x-sqlite3", "application/octet-stream")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_open_db))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { openFolderLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_show_folder))
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
