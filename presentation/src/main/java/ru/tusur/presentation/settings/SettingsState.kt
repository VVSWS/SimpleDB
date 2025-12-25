package ru.tusur.presentation.settings

data class SettingsState(
    val language: SettingsViewModel.Language = SettingsViewModel.Language.EN,
    val theme: SettingsViewModel.Theme = SettingsViewModel.Theme.SYSTEM,
    val message: String? = null
)
