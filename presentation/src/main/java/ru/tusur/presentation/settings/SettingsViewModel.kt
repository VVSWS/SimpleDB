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
import ru.tusur.core.ui.theme.ThemeMode
import ru.tusur.data.backup.ExportDatabaseUseCase
import ru.tusur.data.backup.MergeJsonDatabaseUseCase
import ru.tusur.presentation.shared.AppEvent
import ru.tusur.domain.usecase.database.DatabaseExportProgress
import ru.tusur.presentation.R
import ru.tusur.presentation.util.StringProvider
import ru.tusur.presentation.shared.SharedAppEventsViewModel

// ---------------------------------------------------------
// ViewModel для экрана настроек
// ---------------------------------------------------------
// Управляет:
// - Настройками темы (сохранение в DataStore)
// - Слиянием базы данных из резервной копии
// - Экспортом базы данных в выбранную папку
// - Отображением прогресса длительных операций
class SettingsViewModel(
    private val dataStore: DataStore<Preferences>,        // Хранилище настроек (тема)
    private val mergeDbUseCase: MergeJsonDatabaseUseCase, // UseCase для слияния БД
    private val exportDbUseCase: ExportDatabaseUseCase,   // UseCase для экспорта БД
    private val strings: StringProvider,                  // Провайдер строковых ресурсов
    private val sharedEvents: SharedAppEventsViewModel    // Общие события для уведомлений
) : ViewModel() {

    // ---------------------------------------------------------
    // Состояние экрана настроек
    // ---------------------------------------------------------
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    // ---------------------------------------------------------
    // События экрана настроек
    // ---------------------------------------------------------
    private val _events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SettingsEvent> = _events

    // ---------------------------------------------------------
    // Инициализация: подписка на изменения темы
    // ---------------------------------------------------------
    init {
        observeTheme()
    }

    // ---------------------------------------------------------
    // НАСТРОЙКИ ТЕМЫ
    // ---------------------------------------------------------

    // Подписка на изменения темы из DataStore
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

    // Установка новой темы (сохранение в DataStore)
    fun setTheme(theme: ThemeMode) {
        viewModelScope.launch {
            // Сохранение в DataStore
            dataStore.edit { prefs ->
                prefs[KEY_THEME] = theme.value.toString()
            }
            // Обновление состояния
            _state.update { it.copy(theme = theme) }
        }
    }

    // ---------------------------------------------------------
    // СЛИЯНИЕ БАЗЫ ДАННЫХ С ОТОБРАЖЕНИЕМ ПРОГРЕССА
    // ---------------------------------------------------------
    fun mergeDatabase(folderUri: Uri) {
        viewModelScope.launch {
            try {
                // Сброс индикатора прогресса
                _state.update {
                    it.copy(
                        mergeProgress = 0f,
                        mergeTotalSteps = null
                    )
                }

                // Выполнение слияния в фоновом потоке
                val result = withContext(Dispatchers.IO) {
                    mergeDbUseCase(folderUri) { step, total ->
                        // Обновление прогресса в UI
                        _state.update {
                            it.copy(
                                mergeProgress = step.toFloat() / total.toFloat(),
                                mergeTotalSteps = total
                            )
                        }
                    }
                }

                // Скрытие индикатора прогресса
                _state.update {
                    it.copy(
                        mergeProgress = null,
                        mergeTotalSteps = null
                    )
                }

                // Обработка результата
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    _state.update {
                        it.copy(message = strings.get(R.string.db_merged, count))
                    }
                    // Уведомление главного экрана об изменении данных
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
                // Обработка ошибки
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
    // ЭКСПОРТ БАЗЫ ДАННЫХ С ОТОБРАЖЕНИЕМ ПРОГРЕССА
    // ---------------------------------------------------------
    fun exportDatabaseToFolder(folderUri: Uri) {
        viewModelScope.launch {
            try {
                // Выполнение экспорта в фоновом потоке с отслеживанием прогресса
                val result = withContext(Dispatchers.IO) {
                    exportDbUseCase(
                        folderUri,
                        onProgress = { progress ->
                            when (progress) {
                                // Экспорт начат: известен общий размер
                                is DatabaseExportProgress.Started -> {
                                    _state.update {
                                        it.copy(
                                            exportProgress = 0f,
                                            exportTotalBytes = progress.totalBytes
                                        )
                                    }
                                }

                                // Промежуточный прогресс: обновление процента
                                is DatabaseExportProgress.Progress -> {
                                    val percent = if (progress.totalBytes > 0) {
                                        progress.writtenBytes.toFloat() /
                                                progress.totalBytes.toFloat()
                                    } else 0f

                                    _state.update { it.copy(exportProgress = percent) }
                                }

                                // Экспорт завершён
                                is DatabaseExportProgress.Finished -> {
                                    _state.update { it.copy(exportProgress = 1f) }
                                }

                                // Ошибка при экспорте
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

                // Скрытие индикатора прогресса
                _state.update {
                    it.copy(
                        exportProgress = null,
                        exportTotalBytes = null
                    )
                }

                // Обработка результата
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
                // Обработка ошибки
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
        // Ключ для сохранения выбранной темы в DataStore
        private val KEY_THEME = stringPreferencesKey("theme")
    }
}