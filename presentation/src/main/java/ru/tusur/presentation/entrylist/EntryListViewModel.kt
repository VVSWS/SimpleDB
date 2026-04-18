package ru.tusur.presentation.entrylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.model.SearchFilter
import ru.tusur.domain.usecase.entry.DeleteEntryUseCase
import ru.tusur.domain.usecase.entry.GetEntryByIdUseCase
import ru.tusur.domain.usecase.entry.GetRecentEntriesUseCase
import ru.tusur.domain.usecase.entry.SearchEntriesUseCase
import ru.tusur.presentation.common.EntryListError
import ru.tusur.presentation.shared.AppEvent
import ru.tusur.presentation.shared.SharedAppEventsViewModel

// ---------------------------------------------------------
// ViewModel для экрана списка записей
// ---------------------------------------------------------
// Управляет загрузкой последних записей и результатов поиска
// Обрабатывает удаление записей с уведомлением других экранов
class EntryListViewModel(
    private val getRecentEntriesUseCase: GetRecentEntriesUseCase,  // UseCase для последних записей
    private val searchEntriesUseCase: SearchEntriesUseCase,        // UseCase для поиска
    private val getEntryByIdUseCase: GetEntryByIdUseCase,          // UseCase для получения записи по ID
    private val deleteEntryUseCase: DeleteEntryUseCase,            // UseCase для удаления записи
    private val sharedEvents: SharedAppEventsViewModel             // Общие события для уведомлений
) : ViewModel() {

    // ---------------------------------------------------------
    // UI состояние экрана
    // ---------------------------------------------------------
    data class UiState(
        val entries: List<FaultEntry> = emptyList(),  // Список отображаемых записей
        val isLoading: Boolean = false,               // Флаг загрузки
        val error: EntryListError? = null             // Ошибка (если есть)
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // ---------------------------------------------------------
    // Загрузка последних записей
    // ---------------------------------------------------------
    fun loadRecentEntries() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Получение последних записей (по умолчанию 20)
                val entries = getRecentEntriesUseCase()
                _uiState.value = _uiState.value.copy(
                    entries = entries,
                    isLoading = false
                )
            } catch (e: Exception) {
                // В случае ошибки - отображение сообщения
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = EntryListError.LoadFailed
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Поиск записей по фильтру
    // ---------------------------------------------------------
    fun searchEntries(filter: SearchFilter) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Выполнение поиска с параметрами фильтра
                val entries = searchEntriesUseCase(
                    year = filter.year,
                    brand = filter.brand,
                    model = filter.model,
                    location = filter.location
                )
                _uiState.value = _uiState.value.copy(
                    entries = entries,
                    isLoading = false
                )
            } catch (e: Exception) {
                // В случае ошибки - отображение сообщения
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = EntryListError.SearchFailed
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Удаление записи (корректная версия с полными данными)
    // ---------------------------------------------------------
    fun deleteEntry(entry: FaultEntry) {
        viewModelScope.launch {
            // ---------------------------------------------------------
            // Загрузка полной записи с изображениями
            // ---------------------------------------------------------
            // Важно: entry из списка может не содержать imageUris
            // Для корректного удаления нужно загрузить запись со всеми изображениями
            val fullEntry = getEntryByIdUseCase(entry.id)

            // Удаление полной записи (включая физические файлы изображений)
            if (fullEntry != null) {
                deleteEntryUseCase(fullEntry)
            }

            // ---------------------------------------------------------
            // Обновление локального списка (удаление записи из UI)
            // ---------------------------------------------------------
            _uiState.value = _uiState.value.copy(
                entries = _uiState.value.entries.filterNot { it.id == entry.id }
            )

            // ---------------------------------------------------------
            // Уведомление главного экрана об изменении данных
            // ---------------------------------------------------------
            // Нужно для обновления информации о базе данных (количество записей)
            sharedEvents.emit(AppEvent.EntryChanged)
        }
    }
}