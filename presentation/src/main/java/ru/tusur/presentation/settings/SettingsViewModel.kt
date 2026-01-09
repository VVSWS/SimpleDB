package ru.tusur.presentation.settings

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tusur.data.local.DatabaseProvider
import ru.tusur.domain.usecase.database.CreateDatabaseUseCase
import ru.tusur.domain.usecase.database.DatabaseExportProgress
import ru.tusur.data.backup.ExportDatabaseUseCase
import ru.tusur.presentation.R
import java.io.File
import ru.tusur.presentation.util.StringProvider
import ru.tusur.data.backup.MergeJsonDatabaseUseCase


class SettingsViewModel(
    private val dataStore: DataStore<Preferences>,
    private val createDbUseCase: CreateDatabaseUseCase,
    private val mergeDbUseCase: MergeJsonDatabaseUseCase,
    private val exportDbUseCase: ExportDatabaseUseCase,
    private val provider: DatabaseProvider,
    private val strings: StringProvider
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    private val _events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SettingsEvent> = _events

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
                        theme = Theme.fromValue((prefs[KEY_THEME] ?: "0").toIntOrNull() ?: 0),
                        message = null
                    )
                }
                .collect { _state.value = it }
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
                val file = withContext(Dispatchers.IO) {
                    createDbUseCase()
                }


                // If DB already exists, notify user
                if (file.exists() && file.length() > 0L) {
                    val msg = strings.get(R.string.db_exists)
                    _state.value = _state.value.copy(message = msg)
                    _events.tryEmit(SettingsEvent.DatabaseExists)
                    return@launch
                }

                provider.getDatabase(file)

                val msg = strings.get(R.string.db_created)
                _state.value = _state.value.copy(message = msg)
                _events.tryEmit(SettingsEvent.DatabaseCreated)

            } catch (e: Exception) {
                val msg = strings.get(R.string.db_create_failed, e.message ?: "")
                _state.value = _state.value.copy(message = msg)
                _events.tryEmit(SettingsEvent.DatabaseError(msg))
            }
        }
    }

    fun mergeDatabase(folderUri: Uri) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    mergeDbUseCase(folderUri)
                }

                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    val msg = strings.get(R.string.db_merged, count)
                    _state.value = _state.value.copy(message = msg)
                } else {
                    val msg = strings.get(
                        R.string.db_merge_failed,
                        result.exceptionOrNull()?.message ?: ""
                    )
                    _state.value = _state.value.copy(message = msg)
                }

            } catch (e: Exception) {
                val msg = strings.get(R.string.db_merge_failed, e.message ?: "")
                _state.value = _state.value.copy(message = msg)
            }
        }
    }


    fun exportDatabaseToFolder(folderUri: Uri) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    exportDbUseCase(
                        folderUri,
                        onProgress = { progress ->
                            when (progress) {
                                is DatabaseExportProgress.Started -> {
                                    _state.value = _state.value.copy(exportProgress = 0f)
                                }

                                is DatabaseExportProgress.Progress -> {
                                    val percent = if (progress.totalBytes > 0) {
                                        progress.writtenBytes.toFloat() /
                                                progress.totalBytes.toFloat()
                                    } else 0f

                                    _state.value = _state.value.copy(exportProgress = percent)
                                }

                                is DatabaseExportProgress.Finished -> {
                                    _state.value = _state.value.copy(exportProgress = 1f)
                                }

                                is DatabaseExportProgress.Error -> {
                                    _state.value = _state.value.copy(
                                        message = strings.get(
                                            R.string.settings_db_export_failed,
                                            progress.message
                                        )
                                    )
                                }
                            }
                        }
                    )
                }

                if (result.isSuccess) {
                    _state.value = _state.value.copy(
                        message = strings.get(R.string.settings_db_export_success)
                    )
                } else {
                    _state.value = _state.value.copy(
                        message = strings.get(
                            R.string.settings_db_export_failed,
                            result.exceptionOrNull()?.message ?: ""
                        )
                    )
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = strings.get(
                        R.string.settings_db_export_failed,
                        e.message ?: ""
                    )
                )
            }
        }
    }


    fun exportDatabase(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(exportProgress = 0f)

            val result = exportDbUseCase(uri) { progress ->
                when (progress) {
                    is DatabaseExportProgress.Started -> {
                        _state.value = _state.value.copy(
                            exportProgress = 0f,
                            exportTotalBytes = progress.totalBytes
                        )
                    }

                    is DatabaseExportProgress.Progress -> {
                        val percent = progress.writtenBytes.toFloat() /
                                progress.totalBytes.toFloat()
                        _state.value = _state.value.copy(exportProgress = percent)
                    }

                    is DatabaseExportProgress.Finished -> {
                        _state.value = _state.value.copy(
                            exportProgress = 1f,
                            message = strings.get(R.string.db_exported)
                        )
                    }

                    is DatabaseExportProgress.Error -> {
                        _state.value = _state.value.copy(
                            message = strings.get(
                                R.string.db_export_failed,
                                progress.message
                            )
                        )
                    }

                }

            }

            if (result.isFailure) {
                _state.value = _state.value.copy(
                    message = strings.get( R.string.db_export_failed, result.exceptionOrNull()?.message.orEmpty() )
                )
            }
        }
    }


    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
    }
}
