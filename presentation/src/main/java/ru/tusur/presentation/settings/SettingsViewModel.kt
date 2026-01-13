package ru.tusur.presentation.settings

import android.content.Context
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
import ru.tusur.core.ui.theme.ThemeMode
import ru.tusur.data.backup.ExportDatabaseUseCase
import ru.tusur.data.backup.MergeJsonDatabaseUseCase
import ru.tusur.presentation.shared.AppEvent
import ru.tusur.domain.usecase.database.DatabaseExportProgress
import ru.tusur.presentation.R
import ru.tusur.presentation.util.StringProvider
import ru.tusur.presentation.shared.SharedAppEventsViewModel

class SettingsViewModel(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val mergeDbUseCase: MergeJsonDatabaseUseCase,
    private val exportDbUseCase: ExportDatabaseUseCase,
    private val strings: StringProvider,
    private val sharedEvents: SharedAppEventsViewModel
)
 : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    private val _events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SettingsEvent> = _events

    init {
        observeTheme()
    }

    // ---------------------------------------------------------
    // THEME
    // ---------------------------------------------------------
    private fun observeTheme() {
        viewModelScope.launch {
            dataStore.data
                .map { prefs ->
                    SettingsState(
                        theme = ThemeMode.fromValue(
                            (prefs[KEY_THEME] ?: "0").toIntOrNull() ?: 0
                        )
                    )
                }
                .collect { _state.value = it }
        }
    }

    fun setTheme(theme: ThemeMode) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_THEME] = theme.value.toString()
            }
            _state.update { it.copy(theme = theme) }
        }
    }

    // ---------------------------------------------------------
    // MERGE DATABASE WITH PROGRESS
    // ---------------------------------------------------------
    fun mergeDatabase(folderUri: Uri) {
        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        mergeProgress = 0f,
                        mergeTotalSteps = null
                    )
                }

                val result = withContext(Dispatchers.IO) {
                    mergeDbUseCase(folderUri) { step, total ->
                        _state.update {
                            it.copy(
                                mergeProgress = step.toFloat() / total.toFloat(),
                                mergeTotalSteps = total
                            )
                        }
                    }
                }

                _state.update {
                    it.copy(
                        mergeProgress = null,
                        mergeTotalSteps = null
                    )
                }

                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    _state.update {
                        it.copy(message = strings.get(R.string.db_merged, count))
                    }
                    sharedEvents.emit(AppEvent.DatabaseMerged)
                } else {
                    _state.update {
                        it.copy(
                            message = strings.get(
                                R.string.db_merge_failed,
                                result.exceptionOrNull()?.message ?: ""
                            )
                        )
                    }
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        mergeProgress = null,
                        mergeTotalSteps = null,
                        message = strings.get(R.string.db_merge_failed, e.message ?: "")
                    )
                }
            }
        }
    }

    // ---------------------------------------------------------
    // EXPORT DATABASE WITH PROGRESS
    // ---------------------------------------------------------
    fun exportDatabaseToFolder(folderUri: Uri) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    exportDbUseCase(
                        folderUri,
                        onProgress = { progress ->
                            when (progress) {
                                is DatabaseExportProgress.Started -> {
                                    _state.update {
                                        it.copy(
                                            exportProgress = 0f,
                                            exportTotalBytes = progress.totalBytes
                                        )
                                    }
                                }

                                is DatabaseExportProgress.Progress -> {
                                    val percent = if (progress.totalBytes > 0) {
                                        progress.writtenBytes.toFloat() /
                                                progress.totalBytes.toFloat()
                                    } else 0f

                                    _state.update { it.copy(exportProgress = percent) }
                                }

                                is DatabaseExportProgress.Finished -> {
                                    _state.update { it.copy(exportProgress = 1f) }
                                }

                                is DatabaseExportProgress.Error -> {
                                    _state.update {
                                        it.copy(
                                            message = strings.get(
                                                R.string.settings_db_export_failed,
                                                progress.message
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                _state.update {
                    it.copy(
                        exportProgress = null,
                        exportTotalBytes = null
                    )
                }

                if (result.isSuccess) {
                    _state.update {
                        it.copy(message = strings.get(R.string.settings_db_export_success))
                    }
                } else {
                    _state.update {
                        it.copy(
                            message = strings.get(
                                R.string.settings_db_export_failed,
                                result.exceptionOrNull()?.message ?: ""
                            )
                        )
                    }
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        exportProgress = null,
                        exportTotalBytes = null,
                        message = strings.get(
                            R.string.settings_db_export_failed,
                            e.message ?: ""
                        )
                    )
                }
            }
        }
    }

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
    }
}
