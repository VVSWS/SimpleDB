package ru.tusur.presentation.mainscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// ---------------------------------------------------------
// Главный экран приложения (меню)
// ---------------------------------------------------------
// Отображает карточку с информацией о текущей базе данных
// Содержит кнопки навигации:
// - Последние записи
// - Добавить новую запись
// - Поиск записей
// - Настройки
// - О приложении
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, backStackEntry: NavBackStackEntry) {
    // Внедрение ViewModel с привязкой к backStackEntry (правильная область видимости)
    val viewModel: MainViewModel = koinViewModel(viewModelStoreOwner = backStackEntry)
    val uiState by viewModel.uiState.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }

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
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    // Кнопка настроек
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.main_settings)
                        )
                    }

                    // Кнопка помощи (справка)
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = stringResource(R.string.cd_help)
                        )
                    }

                    // Кнопка "О приложении"
                    IconButton(onClick = { navController.navigate("about") }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.main_about)
                        )
                    }
                }
            )
        }
    ) { padding ->

        // ---------------------------------------------------------
        // Вертикальный список с кнопками меню
        // ---------------------------------------------------------
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Карточка с информацией о базе данных
            item {
                DatabaseInfoCard(uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Кнопка "Последние записи"
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

            // Кнопка "Добавить запись"
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

            // Кнопка "Поиск записей"
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

            // Отступ снизу
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // ---------------------------------------------------------
    // Диалог справки (информация о функциональности главного экрана)
    // ---------------------------------------------------------
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(stringResource(R.string.help_title_mainscreen))
            },
            text = {
                Text(stringResource(R.string.help_information_mainscreen))
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(stringResource(R.string.help_dialog_close_mainscreen))
                }
            }
        )
    }
}

// ---------------------------------------------------------
// Карточка с информацией о текущей базе данных
// ---------------------------------------------------------
@Composable
private fun DatabaseInfoCard(uiState: MainUiState) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок карточки
            Text(
                text = stringResource(R.string.main_db_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Строки с информацией о БД
            InfoRow(label = stringResource(R.string.main_db_name), value = uiState.dbName)
            InfoRow(label = stringResource(R.string.main_entry_count), value = uiState.entryCount.toString())
            InfoRow(label = stringResource(R.string.main_db_size), value = uiState.dbSizeBytes.toReadableSize())
            InfoRow(label = stringResource(R.string.main_image_count), value = uiState.imageCount.toString())
            InfoRow(label = stringResource(R.string.main_images_folder_size), value = uiState.imagesFolderSizeBytes.toReadableSize())
        }
    }
}

// ---------------------------------------------------------
// Строка с парой "название : значение" в две колонки
// ---------------------------------------------------------
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

// ---------------------------------------------------------
// Функция-расширение для форматирования размера в байтах
// ---------------------------------------------------------
// Преобразует Long (байты) в человеко-читаемый формат:
// - < 1 KB для маленьких значений
// - X KB для килобайт
// - X.XX MB для мегабайт
@SuppressLint("DefaultLocale")
private fun Long.toReadableSize(): String {
    if (this <= 0) return "0 B"

    return when {
        this < 1024 -> "< 1 KB"                                    // Меньше 1 КБ
        this < 1024 * 1024 -> "${this / 1024} KB"                 // От 1 КБ до 1 МБ
        else -> String.format("%.2f MB", this / (1024f * 1024f))  // Больше 1 МБ
    }
}