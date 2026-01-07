package ru.tusur.presentation.settings

sealed class SettingsEvent {
    data object DatabaseCreated : SettingsEvent()
    data class DatabaseError(val message: String) : SettingsEvent()
    data object DatabaseOpened : SettingsEvent()
}
