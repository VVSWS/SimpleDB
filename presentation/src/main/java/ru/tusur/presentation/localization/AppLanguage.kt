package ru.tusur.presentation.localization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

class AppLanguageState(initialLocale: Locale) {
    var locale by mutableStateOf(initialLocale)
        private set

    fun updateLocale(newLocale: Locale) {
        locale = newLocale
    }
}

val LocalAppLanguage = staticCompositionLocalOf<AppLanguageState> {
    error("AppLanguageState not provided")
}
