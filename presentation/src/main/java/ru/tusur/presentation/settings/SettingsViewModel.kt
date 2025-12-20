package ru.tusur.presentation.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {

    enum class Language { EN, ES }
    enum class Theme { SYSTEM, LIGHT, DARK }

    data class UiState(
        val language: Language = Language.EN,
        val theme: Theme = Theme.SYSTEM
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun setLanguage(language: Language) {
        _uiState.value = _uiState.value.copy(language = language)
        // TODO: сохранить в DataStore
    }

    fun setTheme(theme: Theme) {
        _uiState.value = _uiState.value.copy(theme = theme)
        // TODO: сохранить в DataStore
    }

    fun createNewDatabase() {
        // TODO: запустить диалог
    }

    fun openExistingDatabase() {
        // TODO: запустить проводник
    }

    fun showDatabaseFolder() {
        // TODO: открыть папку
    }
}