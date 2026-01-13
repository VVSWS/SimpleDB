package ru.tusur.presentation.settings

import ru.tusur.core.ui.theme.ThemeMode

data class SettingsState(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val message: String? = null,

    // MERGE PROGRESS
    val mergeProgress: Float? = null,        // null = hidden
    val mergeTotalSteps: Int? = null,

    // EXPORT PROGRESS
    val exportProgress: Float? = null,       // null = hidden
    val exportTotalBytes: Long? = null
)
 