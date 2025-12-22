package ru.tusur.presentation.settings

sealed interface SettingsEvent {
    data class LanguageChanged(val languageCode: String) : SettingsEvent
    data class DatabaseError(val message: String) : SettingsEvent
    data object DatabaseCreated : SettingsEvent
    data object DatabaseOpened : SettingsEvent
}
