package ru.tusur.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.tusur.domain.usecase.database.CreateDatabaseUseCase
import ru.tusur.domain.usecase.database.OpenDatabaseUseCase
import java.io.File

class SettingsViewModel(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val createDbUseCase: CreateDatabaseUseCase,
    private val openDbUseCase: OpenDatabaseUseCase
) : ViewModel() {

    enum class Language(val code: String) {
        EN("en"), ES("es");

        companion object {
            fun fromCode(code: String) = entries.find { it.code == code } ?: EN
        }
    }

    enum class Theme(val value: Int) {
        SYSTEM(0), LIGHT(1), DARK(2);

        companion object {
            fun fromValue(value: Int) = entries.find { it.value == value } ?: SYSTEM
        }
    }

    data class UiState(
        val language: Language = Language.EN,
        val theme: Theme = Theme.SYSTEM
    )

    var uiState by mutableStateOf(UiState())
        private set

    private val _events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SettingsEvent> = _events

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data
                .map { prefs ->
                    val langCode = prefs[KEY_LANGUAGE] ?: "en"
                    val themeValue = (prefs[KEY_THEME] ?: "0").toIntOrNull() ?: 0
                    UiState(
                        language = Language.fromCode(langCode),
                        theme = Theme.fromValue(themeValue)
                    )
                }
                .collect { state ->
                    uiState = state
                }
        }
    }

    fun setLanguage(language: Language) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_LANGUAGE] = language.code
            }
            _events.emit(SettingsEvent.LanguageChanged(language.code))
            uiState = uiState.copy(language = language)
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_THEME] = theme.value.toString()
            }
            uiState = uiState.copy(theme = theme)
        }
    }

    /**
     * Create new active DB internally: /databases/BD_active.db
     */
    fun createNewDatabase() {
        viewModelScope.launch {
            runCatching {
                createDbUseCase()
            }.onSuccess {
                _events.emit(SettingsEvent.DatabaseCreated)
            }.onFailure { error ->
                _events.emit(
                    SettingsEvent.DatabaseError(
                        "Failed to create database: ${error.message}"
                    )
                )
            }
        }
    }

    /**
     * Called by the screen when the user selects a DB via SAF.
     * We copy it into internal storage as the active DB.
     */
    fun handleDbSelected(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val tempFile = File(context.cacheDir, "imported_db.db")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                openDbUseCase(tempFile)

                tempFile.delete()
            }.onSuccess {
                _events.emit(SettingsEvent.DatabaseOpened)
            }.onFailure { error ->
                _events.emit(
                    SettingsEvent.DatabaseError(
                        "Failed to open database: ${error.message}"
                    )
                )
            }
        }
    }

    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_THEME = stringPreferencesKey("theme")
    }
}
