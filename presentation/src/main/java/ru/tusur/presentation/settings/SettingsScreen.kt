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
import ru.tusur.core.util.FileHelper
import ru.tusur.presentation.R

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()

    val pickDbForMerge = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = FileHelper.copyUriToTempFile(context, it)
            viewModel.mergeDatabase(file)
        }
    }

    val exportDbLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportDatabase(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.LanguageChanged -> Unit
                is SettingsEvent.DatabaseError -> Unit
                is SettingsEvent.DatabaseCreated -> Unit
                is SettingsEvent.DatabaseOpened -> Unit
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
                selected = uiState.language == SettingsViewModel.Language.EN,
                onClick = { viewModel.setLanguage(SettingsViewModel.Language.EN) }
            )

            LanguageRadioButton(
                label = stringResource(R.string.settings_language_es),
                selected = uiState.language == SettingsViewModel.Language.ES,
                onClick = { viewModel.setLanguage(SettingsViewModel.Language.ES) }
            )

            LanguageRadioButton(
                label = stringResource(R.string.settings_language_ru),
                selected = uiState.language == SettingsViewModel.Language.RU,
                onClick = { viewModel.setLanguage(SettingsViewModel.Language.RU) }
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Text(
                text = stringResource(R.string.settings_db),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Button(
                onClick = { viewModel.createNewDatabase() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_db_create))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { pickDbForMerge.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_db_merge))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { exportDbLauncher.launch("carfault_export.db") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_db_export))
            }

            Spacer(modifier = Modifier.height(16.dp))

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
