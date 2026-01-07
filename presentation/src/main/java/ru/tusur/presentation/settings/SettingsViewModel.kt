package ru.tusur.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.tusur.data.local.DatabaseProvider
import ru.tusur.domain.usecase.database.CreateDatabaseUseCase
import ru.tusur.domain.usecase.database.ExportDatabaseUseCase
import ru.tusur.domain.usecase.database.MergeDatabaseUseCase
import java.io.File

class SettingsViewModel(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val createDbUseCase: CreateDatabaseUseCase,
    private val mergeDbUseCase: MergeDatabaseUseCase,
    private val exportDbUseCase: ExportDatabaseUseCase,
    private val provider: DatabaseProvider
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    private val _events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SettingsEvent> = _events

    enum class Language(val code: String) {
        EN("en"),
        ES("es"),
        RU("ru");

        companion object {
            fun fromCode(code: String) = entries.find { it.code == code } ?: EN
        }
    }

    enum class Theme(val value: Int) {
        SYSTEM(0),
        LIGHT(1),
        DARK(2);

        companion object {
            fun fromValue(value: Int) = entries.find { it.value == value } ?: SYSTEM
        }
    }

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data
                .map { prefs ->
                    SettingsState(
                        language = Language.fromCode(prefs[KEY_LANGUAGE] ?: "en"),
                        theme = Theme.fromValue((prefs[KEY_THEME] ?: "0").toIntOrNull() ?: 0),
                        message = null
                    )
                }
                .collect { _state.value = it }
        }
    }

    fun setLanguage(language: Language) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_LANGUAGE] = language.code
            }
            _state.value = _state.value.copy(language = language)
            _events.tryEmit(SettingsEvent.LanguageChanged(language.code))
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_THEME] = theme.value.toString()
            }
            _state.value = _state.value.copy(theme = theme)
        }
    }

    fun createNewDatabase() {
        viewModelScope.launch {
            try {
                val file = createDbUseCase()
                provider.getDatabase(file)
                _state.value = _state.value.copy(message = "New database created")
                _events.tryEmit(SettingsEvent.DatabaseCreated)
            } catch (e: Exception) {
                val msg = "Failed to create database: ${e.message}"
                _state.value = _state.value.copy(message = msg)
                _events.tryEmit(SettingsEvent.DatabaseError(msg))
            }
        }
    }

    fun mergeDatabase(file: File) {
        viewModelScope.launch {
            try {
                val count = mergeDbUseCase(file)
                _state.value = _state.value.copy(message = "Merged $count entries")
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Merge failed: ${e.message}")
            }
        }
    }

    fun exportDatabase(uri: Uri) {
        viewModelScope.launch {
            val result = exportDbUseCase(uri)
            _state.value = if (result.isSuccess) {
                _state.value.copy(message = "Database exported")
            } else {
                _state.value.copy(
                    message = "Export failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_THEME = stringPreferencesKey("theme")
    }
}
