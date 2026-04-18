package ru.tusur.carfault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.core.view.WindowCompat
import org.koin.androidx.compose.koinViewModel
import ru.tusur.core.ui.theme.CarFaultTheme
import ru.tusur.core.ui.theme.ThemeMode
import ru.tusur.presentation.settings.SettingsViewModel

// ---------------------------------------------------------
// Главная Activity приложения (точка входа в Android)
// ---------------------------------------------------------
// Использует Jetpack Compose для построения интерфейса
// Отвечает за настройку системного бара (статус-бар и навигация)
class MainActivity : ComponentActivity() {

    // ---------------------------------------------------------
    // Создание Activity
    // ---------------------------------------------------------
    // Вызывается при запуске Activity
    // Настройка темы, системных баров и отображение Compose-интерфейса
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---------------------------------------------------------
        // Установка Compose-содержимого
        // ---------------------------------------------------------
        setContent {
            // ---------------------------------------------------------
            // Получение ViewModel настроек через Koin
            // ---------------------------------------------------------
            // ViewModel сохраняет состояние темы (системная/светлая/тёмная)
            val settingsViewModel: SettingsViewModel = koinViewModel()

            // ---------------------------------------------------------
            // Подписка на состояние настроек как поток (StateFlow)
            // ---------------------------------------------------------
            // collectAsState() преобразует Flow в Compose State
            // При изменении темы UI автоматически перерисовывается
            val uiState = settingsViewModel.state.collectAsState()

            // ---------------------------------------------------------
            // Определение тёмной темы на основе выбора пользователя
            // ---------------------------------------------------------
            val darkTheme = when (uiState.value.theme) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()  // Следовать системным настройкам
                ThemeMode.LIGHT -> false                    // Принудительно светлая
                ThemeMode.DARK -> true                      // Принудительно тёмная
            }

            // ---------------------------------------------------------
            // Настройка системного окна (отключение обрезки под системные бары)
            // ---------------------------------------------------------
            // Позволяет контенту рисоваться под статус-баром и навигационной панелью
            val window = this.window
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // ---------------------------------------------------------
            // Контроллер для управления системными барами
            // ---------------------------------------------------------
            // Позволяет менять цвета иконок статус-бара и навигационной панели
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)

            // ---------------------------------------------------------
            // Побочный эффект для обновления стиля системных баров
            // ---------------------------------------------------------
            // SideEffect выполняется после каждого успешного перерисовывания композиции
            // Меняет цвет иконок в зависимости от темы (светлые/тёмные иконки)
            SideEffect {
                // Статус-бар: светлые иконки при тёмной теме, тёмные при светлой
                insetsController.isAppearanceLightStatusBars = !darkTheme
                // Навигационная панель: светлые иконки при тёмной теме, тёмные при светлой
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }

            // ---------------------------------------------------------
            // Применение темы приложения
            // ---------------------------------------------------------
            // CarFaultTheme оборачивает MaterialTheme с кастомными цветами
            // Передаётся выбранный режим темы (системная/светлая/тёмная)
            CarFaultTheme(themeMode = uiState.value.theme) {
                // ---------------------------------------------------------
                // Запуск главного экрана приложения
                // ---------------------------------------------------------
                // Содержит всю навигацию между экранами
                CarFaultApp()
            }
        }
    }
}