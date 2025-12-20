package ru.tusur.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import ru.tusur.stop.presentation.R

@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row {
                    RadioButton(
                        selected = uiState.language == SettingsViewModel.Language.EN,
                        onClick = { viewModel.setLanguage(SettingsViewModel.Language.EN) }
                    )
                    Text(
                        text = stringResource(R.string.settings_language_en),
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Row {
                    RadioButton(
                        selected = uiState.language == SettingsViewModel.Language.ES,
                        onClick = { viewModel.setLanguage(SettingsViewModel.Language.ES) }
                    )
                    Text(
                        text = stringResource(R.string.settings_language_es),
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                listOf(
                    SettingsViewModel.Theme.SYSTEM to R.string.settings_theme_system,
                    SettingsViewModel.Theme.LIGHT to R.string.settings_theme_light,
                    SettingsViewModel.Theme.DARK to R.string.settings_theme_dark
                ).forEach { (theme, stringRes) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.theme == theme,
                            onClick = { viewModel.setTheme(theme) }
                        )
                        Text(
                            text = stringResource(stringRes),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

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
                    onClick = { viewModel.openExistingDatabase() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_open_db))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.showDatabaseFolder() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_show_folder))
                }
            }
        }
    )
}