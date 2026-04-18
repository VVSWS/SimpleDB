package ru.tusur.carfault

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.androidx.compose.koinViewModel
import ru.tusur.presentation.about.AboutScreen
import ru.tusur.presentation.entryedit.EditEntryDescriptionScreen
import ru.tusur.presentation.entrylist.EntryListScreen
import ru.tusur.presentation.entrynewmetadata.NewEntryMetadataScreen
import ru.tusur.presentation.entrysearch.EntrySearchScreen
import ru.tusur.presentation.entryview.RecordingViewScreen
import ru.tusur.presentation.mainscreen.MainScreen
import ru.tusur.presentation.mainscreen.MainViewModel
import ru.tusur.presentation.settings.SettingsScreen
import ru.tusur.presentation.settings.SettingsViewModel

// ---------------------------------------------------------
// Главный экран приложения (точка входа в Compose UI)
// ---------------------------------------------------------
// Определяет навигацию между всеми экранами приложения
@Composable
fun CarFaultApp() {
    // ---------------------------------------------------------
    // Корневая поверхность с фоном темы Material 3
    // ---------------------------------------------------------
    // Оборачивает весь интерфейс в Surface для единого стиля
    Surface(
        modifier = Modifier
            .fillMaxSize(),          // Растягивание на весь доступный размер
        color = MaterialTheme.colorScheme.background  // Цвет фона из текущей темы
    ) {
        // ---------------------------------------------------------
        // Контроллер навигации
        // ---------------------------------------------------------
        // Управление перемещением между экранами и стеком назад
        val navController = rememberNavController()

        // ---------------------------------------------------------
        // Хост навигации с графом маршрутов
        // ---------------------------------------------------------
        // Стартовый экран - "main" (главный экран)
        NavHost(navController = navController, startDestination = "main") {

            // ---------------------------------------------------------
            // Маршрут: главный экран
            // ---------------------------------------------------------
            // Содержит основное меню и общую информацию
            composable("main") { backStackEntry ->
                MainScreen(
                    navController = navController,
                    backStackEntry = backStackEntry
                )
            }

            // ---------------------------------------------------------
            // Маршрут: список последних записей
            // ---------------------------------------------------------
            // Отображение недавно добавленных записей о неисправностях
            composable("recent_entries") {
                EntryListScreen(navController, isSearchMode = false)
            }

            // ---------------------------------------------------------
            // Маршрут: результаты поиска записей
            // ---------------------------------------------------------
            // Отображение найденных по критериям записей
            composable("search_entries") {
                EntryListScreen(navController, isSearchMode = true)
            }

            // ---------------------------------------------------------
            // Маршрут: создание новой записи (выбор метаданных)
            // ---------------------------------------------------------
            // Выбор марки, модели, года и места для новой записи
            composable("new_metadata") {
                NewEntryMetadataScreen(navController)
            }

            // ---------------------------------------------------------
            // Маршрут: расширенный поиск
            // ---------------------------------------------------------
            // Форма с фильтрами по марке, модели, году и локации
            composable("search") {
                EntrySearchScreen(navController)
            }

            // ---------------------------------------------------------
            // Маршрут: настройки приложения
            // ---------------------------------------------------------
            // Управление базой данных, импорт/экспорт, тема
            composable("settings") { backStackEntry ->
                // Получение ViewModel для экрана настроек через Koin
                val settingsViewModel: SettingsViewModel = koinViewModel(
                    viewModelStoreOwner = backStackEntry
                )
                // Получение главной ViewModel для обновления состояния
                val mainViewModel: MainViewModel = koinViewModel()

                SettingsScreen(
                    navController = navController,
                    viewModel = settingsViewModel,
                    mainViewModel = mainViewModel
                )
            }

            // ---------------------------------------------------------
            // Маршрут: информация о приложении
            // ---------------------------------------------------------
            // Отображение версии, авторов, лицензии
            composable("about") {
                AboutScreen(navController)
            }

            // ---------------------------------------------------------
            // Маршрут: просмотр записи по ID
            // ---------------------------------------------------------
            // Параметр id передаётся в маршруте как целое число (Long)
            composable(
                route = "view_entry/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                // Извлечение ID записи из аргументов навигации
                val id = entry.arguments!!.getLong("id")
                RecordingViewScreen(navController, id)
            }

            // ---------------------------------------------------------
            // Маршрут: редактирование описания записи
            // ---------------------------------------------------------
            // Параметр entryId передаётся в маршруте как целое число (Long)
            composable(
                route = "edit_entry_description/{entryId}",
                arguments = listOf(navArgument("entryId") { type = NavType.LongType })
            ) { backStackEntry ->
                // Извлечение ID записи из аргументов навигации
                val entryId = backStackEntry.arguments!!.getLong("entryId")

                EditEntryDescriptionScreen(
                    navController = navController,
                    entryId = entryId
                )
            }
        }
    }
}