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

// ---------------------------------------------------------
// Экран настроек приложения
// ---------------------------------------------------------
// Позволяет пользователю:
// - Выбрать тему оформления (системная, светлая, тёмная)
// - Выполнить слияние базы данных из резервной копии
// - Экспортировать текущую базу данных в выбранную папку
// - Сбросить все данные (удалить записи и изображения)
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

    // ---------------------------------------------------------
    // Лаунчер для выбора папки слияния (OpenDocumentTree)
    // ---------------------------------------------------------
    val mergeFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { viewModel.mergeDatabase(it) }
    }

    // ---------------------------------------------------------
    // Лаунчер для выбора папки экспорта (OpenDocumentTree)
    // ---------------------------------------------------------
    val exportFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { viewModel.exportDatabaseToFolder(it) }
    }

    // ---------------------------------------------------------
    // Обработка событий настроек (пока пустая, зарезервирована)
    // ---------------------------------------------------------
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.DatabaseCreated -> {}
                is SettingsEvent.DatabaseError -> {}
            }
        }
    }

    // ---------------------------------------------------------
    // Уведомление об успешном сбросе базы данных
    // ---------------------------------------------------------
    LaunchedEffect(mainUiState.resetCompleted) {
        if (mainUiState.resetCompleted) {
            Toast.makeText(
                context,
                context.getString(R.string.settings_db_reset_success),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ---------------------------------------------------------
    // Структура экрана: TopBar + контент
    // ---------------------------------------------------------
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
                    // Кнопка возврата
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    // Кнопка помощи (справка)
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

        // ---------------------------------------------------------
        // Основной контент с прокруткой
        // ---------------------------------------------------------
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // ---------------------------------------------------------
            // СЕКЦИЯ: НАСТРОЙКИ ТЕМЫ
            // ---------------------------------------------------------
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Системная тема (следует за настройками ОС)
            ThemeRadioButton(
                label = stringResource(R.string.settings_theme_system),
                selected = uiState.theme == ThemeMode.SYSTEM,
                onClick = { viewModel.setTheme(ThemeMode.SYSTEM) }
            )

            // Светлая тема (принудительно)
            ThemeRadioButton(
                label = stringResource(R.string.settings_theme_light),
                selected = uiState.theme == ThemeMode.LIGHT,
                onClick = { viewModel.setTheme(ThemeMode.LIGHT) }
            )

            // Тёмная тема (принудительно)
            ThemeRadioButton(
                label = stringResource(R.string.settings_theme_dark),
                selected = uiState.theme == ThemeMode.DARK,
                onClick = { viewModel.setTheme(ThemeMode.DARK) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------------------------------------------------
            // СЕКЦИЯ: УПРАВЛЕНИЕ БАЗОЙ ДАННЫХ
            // ---------------------------------------------------------
            Text(
                text = stringResource(R.string.settings_db),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // ---------------------------------------------------------
            // КНОПКА СЛИЯНИЯ БАЗЫ ДАННЫХ
            // ---------------------------------------------------------
            Button(
                onClick = { mergeFolderLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.mergeProgress == null && uiState.exportProgress == null
            ) {
                Text(stringResource(R.string.settings_db_merge))
            }

            // Индикатор прогресса слияния
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

            // ---------------------------------------------------------
            // КНОПКА ЭКСПОРТА БАЗЫ ДАННЫХ
            // ---------------------------------------------------------
            Button(
                onClick = { exportFolderLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.mergeProgress == null && uiState.exportProgress == null
            ) {
                Text(stringResource(R.string.settings_db_export))
            }

            // Индикатор прогресса экспорта
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

            // ---------------------------------------------------------
            // КНОПКА СБРОСА БАЗЫ ДАННЫХ (С ПОДТВЕРЖДЕНИЕМ)
            // ---------------------------------------------------------
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

            // Диалог подтверждения сброса
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

            // ---------------------------------------------------------
            // СООБЩЕНИЯ (информационные или об ошибках)
            // ---------------------------------------------------------
            uiState.message?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Диалог справки (информация о функциональности экрана настроек)
    // ---------------------------------------------------------
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

// ---------------------------------------------------------
// Компонент выбора темы (радиокнопка с текстом)
// ---------------------------------------------------------
@Composable
private fun ThemeRadioButton(
    label: String,      // Текст метки
    selected: Boolean,  // Выбран ли данный вариант
    onClick: () -> Unit // Обработчик нажатия
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }  // Вся строка кликабельна
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