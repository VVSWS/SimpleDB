package ru.tusur.presentation.entryview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.usecase.entry.DeleteEntryUseCase
import ru.tusur.domain.usecase.entry.GetEntryByIdUseCase
import ru.tusur.domain.repository.FaultRepository
import ru.tusur.presentation.shared.AppEvent
import ru.tusur.presentation.shared.SharedAppEventsViewModel

// ---------------------------------------------------------
// ViewModel для экрана просмотра деталей записи
// ---------------------------------------------------------
// Управляет загрузкой, отображением, редактированием и удалением записи
// Поддерживает:
// - Загрузку записи с изображениями
// - Обновление описания
// - Удаление отдельных изображений
// - Полное удаление записи
// - Уведомление других экранов об изменениях через SharedAppEventsViewModel
class RecordingViewViewModel(
    private val repository: FaultRepository,                 // Репозиторий для работы с записями
    private val getEntryByIdUseCase: GetEntryByIdUseCase,   // UseCase для получения записи по ID
    private val deleteEntryUseCase: DeleteEntryUseCase,     // UseCase для удаления записи
    private val sharedEvents: SharedAppEventsViewModel,     // Общие события для уведомлений
    private val entryId: Long                               // ID отображаемой записи
) : ViewModel() {

    // ---------------------------------------------------------
    // UI состояние экрана
    // ---------------------------------------------------------
    private val _state = MutableStateFlow(RecordingViewUiState())
    val state: StateFlow<RecordingViewUiState> = _state

    // ---------------------------------------------------------
    // Инициализация: загрузка записи
    // ---------------------------------------------------------
    init {
        loadEntry()
    }

    // ---------------------------------------------------------
    // Загрузка записи из репозитория
    // ---------------------------------------------------------
    private fun loadEntry() {
        viewModelScope.launch {
            try {
                // Установка флага загрузки
                _state.value = _state.value.copy(isLoading = true)

                // Получение записи со всеми данными для просмотра
                val entry = repository.getEntryWithRecording(entryId)

                // Обновление состояния с загруженной записью
                _state.value = RecordingViewUiState(entry = entry)
            } catch (e: Exception) {
                // В случае ошибки - сохранение сообщения
                _state.value = RecordingViewUiState(error = e.message ?: "Unknown error")
            }
        }
    }

    // ---------------------------------------------------------
    // Обновление данных (при возврате на экран)
    // ---------------------------------------------------------
    fun refresh() = loadEntry()

    // ---------------------------------------------------------
    // Удаление отдельного изображения по URI
    // ---------------------------------------------------------
    fun deleteImage(uri: String) {
        viewModelScope.launch {
            try {
                // Удаление физического файла и связей в БД
                repository.deleteImage(uri)

                // Обновление состояния после удаления изображения
                val updated = repository.getEntryWithRecording(entryId)
                _state.value = _state.value.copy(entry = updated)

            } catch (e: Exception) {
                // Сохранение сообщения об ошибке
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to delete image"
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Обновление описания записи
    // ---------------------------------------------------------
    fun updateDescription(newDescription: String) {
        val current = _state.value.entry ?: return

        // Создание обновлённой записи с новым описанием
        val updatedEntry = FaultEntry(
            id = current.id,
            title = current.title,
            year = current.year,
            brand = current.brand,
            model = current.model,
            location = current.location,
            timestamp = current.timestamp,
            description = newDescription,
            imageUris = current.imageUris
        )

        viewModelScope.launch {
            try {
                // Сохранение обновлённой записи в БД
                repository.updateEntry(updatedEntry)

                // Обновление состояния после сохранения
                val refreshed = repository.getEntryWithRecording(entryId)
                _state.value = _state.value.copy(entry = refreshed)

            } catch (e: Exception) {
                // Сохранение сообщения об ошибке
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to update description"
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Полное удаление записи (корректная версия)
    // ---------------------------------------------------------
    fun deleteEntry() {
        val e = _state.value.entry ?: return

        viewModelScope.launch {
            try {
                // ---------------------------------------------------------
                // Загрузка полной записи с изображениями
                // ---------------------------------------------------------
                // Важно: EntryWithRecording может не содержать все URI изображений
                // для корректного удаления нужно загрузить запись со всеми изображениями
                val fullEntry = getEntryByIdUseCase(e.id)

                // Удаление полной записи (включая физические файлы изображений)
                if (fullEntry != null) {
                    deleteEntryUseCase(fullEntry)
                }

                // Установка флага, что запись удалена (для закрытия экрана)
                _state.value = _state.value.copy(isDeleted = true)

                // Уведомление главного экрана об изменении данных
                sharedEvents.emit(AppEvent.EntryChanged)

            } catch (ex: Exception) {
                // Сохранение сообщения об ошибке
                _state.value = _state.value.copy(
                    error = ex.message ?: "Failed to delete entry"
                )
            }
        }
    }
}

// ---------------------------------------------------------
// UI состояние экрана просмотра записи
// ---------------------------------------------------------
data class RecordingViewUiState(
    val isLoading: Boolean = false,                    // Флаг загрузки
    val entry: EntryWithRecording? = null,             // Отображаемая запись
    val error: String? = null,                         // Сообщение об ошибке
    val isDeleted: Boolean = false                     // Флаг удаления (для закрытия экрана)
)