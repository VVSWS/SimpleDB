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


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Get theme from settings VM (or a dedicated ThemeViewModel)
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val uiState = settingsViewModel.state.collectAsState()
            val darkTheme = when (uiState.value.theme) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val window = this.window
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val insetsController = WindowCompat.getInsetsController(window, window.decorView)

            SideEffect {
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }

            CarFaultTheme(themeMode = uiState.value.theme) {
                CarFaultApp()
            }
        }
    }
}
