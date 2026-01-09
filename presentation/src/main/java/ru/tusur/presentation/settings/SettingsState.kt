package ru.tusur.presentation.settings

data class SettingsState(
    val theme: SettingsViewModel.Theme = SettingsViewModel.Theme.SYSTEM,
    val message: String? = null,
    val exportProgress: Float = 0f, // 0.0–1.0
    val exportTotalBytes: Long = 0L

)
