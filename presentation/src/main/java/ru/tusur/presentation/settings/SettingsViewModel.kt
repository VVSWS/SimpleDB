package ru.tusur.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.tusur.core.util.FileHelper
import ru.tusur.data.local.MergeDatabaseManager
import ru.tusur.domain.usecase.database.CreateDatabaseUseCase
import ru.tusur.domain.usecase.database.OpenDatabaseUseCase
import java.io.File

class SettingsViewModel(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val createDbUseCase: CreateDatabaseUseCase,
    private val openDbUseCase: OpenDatabaseUseCase,
    private val mergeManager: MergeDatabaseManager
) : ViewModel() {

    // -------------------------
    // SINGLE SOURCE OF TRUTH
    // -------------------------
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    // -------------------------
    // EVENTS
    // -------------------------
    private val _events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SettingsEvent> = _events

    // -------------------------
    // ENUMS
    // -------------------------
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

    init {
        loadSettings()
    }

    // -------------------------
    // LOAD SETTINGS
    // -------------------------
    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data
                .map { prefs ->
                    val langCode = prefs[KEY_LANGUAGE] ?: "en"
                    val themeValue = (prefs[KEY_THEME] ?: "0").toIntOrNull() ?: 0

                    SettingsState(
                        language = Language.fromCode(langCode),
                        theme = Theme.fromValue(themeValue)
                    )
                }
                .collect { newState ->
                    _state.value = newState
                }
        }
    }

    // -------------------------
    // LANGUAGE + THEME
    // -------------------------
    fun setLanguage(language: Language) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_LANGUAGE] = language.code
            }
            _state.value = _state.value.copy(language = language)
            _events.emit(SettingsEvent.LanguageChanged(language.code))
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

    // -------------------------
    // DATABASE OPERATIONS
    // -------------------------

    fun createNewDatabase() {
        viewModelScope.launch {
            try {
                val file = createDbUseCase()
                _state.value = _state.value.copy(
                    message = "New database created: ${file.name}"
                )
                _events.emit(SettingsEvent.DatabaseCreated)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.DatabaseError("Failed: ${e.message}"))
            }
        }
    }

    fun handleDbSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                val tempFile = File(context.cacheDir, "imported_db.db")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                openDbUseCase(tempFile)
                tempFile.delete()

                _events.emit(SettingsEvent.DatabaseOpened)

            } catch (e: Exception) {
                _events.emit(SettingsEvent.DatabaseError("Failed: ${e.message}"))
            }
        }
    }

    fun openDatabase(file: File) {
        viewModelScope.launch {
            try {
                val active = openDbUseCase(file)
                _state.value = _state.value.copy(
                    message = "Database opened: ${active.name}"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "Failed to open database: ${e.message}"
                )
            }
        }
    }

    fun exportDatabase(dest: File) {
        viewModelScope.launch {
            try {
                val active = FileHelper.getActiveDatabaseFile(context)
                FileHelper.copyFile(active, dest)
                _state.value = _state.value.copy(
                    message = "Exported to ${dest.absolutePath}"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "Export failed: ${e.message}"
                )
            }
        }
    }

    fun mergeDatabase(file: File) {
        viewModelScope.launch {
            try {
                val count = mergeManager.merge(file)
                _state.value = _state.value.copy(
                    message = "Merged $count entries"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "Merge failed: ${e.message}"
                )
            }
        }
    }

    fun recoverDatabase() {
        viewModelScope.launch {
            val file = FileHelper.getActiveDatabaseFile(context)
            if (file.exists()) file.delete()
            createNewDatabase()
        }
    }

    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_THEME = stringPreferencesKey("theme")
    }
}
