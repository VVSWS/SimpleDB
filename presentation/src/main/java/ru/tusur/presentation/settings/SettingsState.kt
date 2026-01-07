package ru.tusur.presentation.settings

data class SettingsState(
    val theme: SettingsViewModel.Theme = SettingsViewModel.Theme.SYSTEM,
    val message: String? = null
)
