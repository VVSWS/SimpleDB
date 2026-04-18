package ru.tusur.presentation.entryedit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.tusur.core.files.ImageStorage
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.usecase.entry.CreateEntryUseCase
import ru.tusur.domain.usecase.entry.DeleteEntryUseCase
import ru.tusur.domain.usecase.entry.GetEntryByIdUseCase
import ru.tusur.domain.usecase.entry.UpdateEntryUseCase
import ru.tusur.presentation.common.DescriptionError
import ru.tusur.presentation.shared.AppEvent
import ru.tusur.presentation.shared.SharedAppEventsViewModel

// ---------------------------------------------------------
// ViewModel для экрана редактирования описания записи
// ---------------------------------------------------------
// Управляет загрузкой, редактированием и сохранением записи
// Поддерживает добавление и удаление изображений
// Взаимодействует с SharedAppEventsViewModel для уведомления других экранов об изменениях
class EditEntryDescriptionViewModel(
    private val id: Long,                              // ID редактируемой записи (0 для новой)
    private val getEntryById: GetEntryByIdUseCase,     // UseCase для загрузки записи
    private val createEntry: CreateEntryUseCase,       // UseCase для создания новой записи
    private val updateEntry: UpdateEntryUseCase,       // UseCase для обновления существующей
    private val deleteEntryUseCase: DeleteEntryUseCase, // UseCase для удаления записи
    private val sharedEvents: SharedAppEventsViewModel // Общие события для уведомления экранов
) : ViewModel() {

    // ---------------------------------------------------------
    // UI состояние экрана
    // ---------------------------------------------------------
    data class UiState(
        val entry: FaultEntry? = null,           // Текущая редактируемая запись
        val isLoading: Boolean = false,          // Флаг загрузки данных
        val isSaving: Boolean = false,           // Флаг сохранения
        val saveCompleted: Boolean = false,      // Флаг завершения сохранения
        val descriptionError: DescriptionError? = null, // Ошибка валидации описания
        val showSaveSuccess: Boolean = false     // Флаг показа диалога успеха
    ) {
        // Валидность формы: запись загружена, описание не пустое, нет ошибок
        val isValid: Boolean
            get() = entry != null &&
                    descriptionError == null &&
                    entry.description.isNotBlank()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // ---------------------------------------------------------
    // Инициализация: загрузка записи по ID
    // ---------------------------------------------------------
    init {
        loadEntry()
    }

    // ---------------------------------------------------------
    // Загрузка записи из репозитория
    // ---------------------------------------------------------
    private fun loadEntry() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val entry = getEntryById(id)

            _uiState.value = if (entry != null) {
                _uiState.value.copy(
                    entry = entry,
                    isLoading = false
                )
            } else {
                // Если запись не найдена (только для существующих ID)
                _uiState.value.copy(
                    isLoading = false,
                    descriptionError = DescriptionError.Empty
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Обработка изменения текста описания
    // ---------------------------------------------------------
    fun onDescriptionChanged(description: String) {
        val current = _uiState.value.entry ?: return

        // Валидация: описание не должно быть пустым
        val error = if (description.isBlank()) DescriptionError.Empty else null

        _uiState.value = _uiState.value.copy(
            entry = current.copy(description = description),
            descriptionError = error
        )
    }

    // ---------------------------------------------------------
    // Обработка выбора изображений из галереи
    // ---------------------------------------------------------
    fun onImagesSelected(context: Context, uris: List<Uri>) {
        val current = _uiState.value.entry ?: return

        // Сохранение выбранных изображений в приватное хранилище
        val newPaths = uris.map { ImageStorage.savePickedImage(context, it) }

        // Добавление новых путей к существующему списку
        _uiState.value = _uiState.value.copy(
            entry = current.copy(
                imageUris = current.imageUris + newPaths
            )
        )
    }

    // ---------------------------------------------------------
    // Удаление изображения
    // ---------------------------------------------------------
    fun removeImage(context: Context, path: String) {
        val current = _uiState.value.entry ?: return

        viewModelScope.launch {
            try {
                // 1. Удаление физического файла изображения
                ImageStorage.deleteImageFile(context, path)

                // 2. Создание обновлённой записи без удалённого изображения
                val updatedEntry = current.copy(
                    imageUris = current.imageUris - path
                )

                // 3. Немедленное сохранение изменений в БД
                updateEntry(updatedEntry)

                // 4. Обновление UI состояния
                _uiState.value = _uiState.value.copy(entry = updatedEntry)

                // 5. Уведомление главного экрана об изменении (обновление списка)
                sharedEvents.emit(AppEvent.EntryChanged)

            } catch (e: Exception) {
                // Опционально: обработка ошибки
            }
        }
    }

    // ---------------------------------------------------------
    // Сохранение записи (обновление)
    // ---------------------------------------------------------
    fun saveEntry() {
        val state = _uiState.value
        val entry = state.entry ?: return
        if (!state.isValid) return  // Проверка валидности перед сохранением

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                // Сохранение записи в БД
                updateEntry(entry)

                // Уведомление главного экрана об изменении
                sharedEvents.emit(AppEvent.EntryChanged)

                // Обновление состояния: сохранение завершено, показать диалог успеха
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveCompleted = true,
                    showSaveSuccess = true
                )

            } catch (e: Exception) {
                // В случае ошибки - сброс флага сохранения
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    // ---------------------------------------------------------
    // Сброс флага завершения сохранения
    // ---------------------------------------------------------
    fun consumeSaveCompleted() {
        _uiState.value = _uiState.value.copy(saveCompleted = false)
    }

    // ---------------------------------------------------------
    // Скрытие диалога успешного сохранения
    // ---------------------------------------------------------
    fun dismissSaveSuccess() {
        _uiState.value = _uiState.value.copy(showSaveSuccess = false)
    }
}