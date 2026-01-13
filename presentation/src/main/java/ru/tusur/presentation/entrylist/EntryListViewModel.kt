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

class EntryListViewModel(
    private val getRecentEntriesUseCase: GetRecentEntriesUseCase,
    private val searchEntriesUseCase: SearchEntriesUseCase,
    private val getEntryByIdUseCase: GetEntryByIdUseCase,
    private val deleteEntryUseCase: DeleteEntryUseCase,
    private val sharedEvents: SharedAppEventsViewModel
) : ViewModel() {

    data class UiState(
        val entries: List<FaultEntry> = emptyList(),
        val isLoading: Boolean = false,
        val error: EntryListError? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // ---------------------------------------------------------
    // Load recent entries
    // ---------------------------------------------------------
    fun loadRecentEntries() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val entries = getRecentEntriesUseCase()
                _uiState.value = _uiState.value.copy(
                    entries = entries,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = EntryListError.LoadFailed
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Search entries
    // ---------------------------------------------------------
    fun searchEntries(filter: SearchFilter) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = EntryListError.SearchFailed
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Delete entry (correct version)
    // ---------------------------------------------------------
    fun deleteEntry(entry: FaultEntry) {
        viewModelScope.launch {
            // Load full entry with imageUris
            val fullEntry = getEntryByIdUseCase(entry.id)

            if (fullEntry != null) {
                deleteEntryUseCase(fullEntry)
            }

            // Update local list
            _uiState.value = _uiState.value.copy(
                entries = _uiState.value.entries.filterNot { it.id == entry.id }
            )

            // Notify main screen to refresh DB info
            sharedEvents.emit(AppEvent.EntryChanged)
        }
    }
}
