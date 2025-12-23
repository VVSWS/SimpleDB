package ru.tusur.presentation.entrylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.usecase.entry.DeleteEntryUseCase
import ru.tusur.domain.usecase.entry.GetRecentEntriesUseCase
import ru.tusur.domain.usecase.entry.SearchEntriesUseCase

class EntryListViewModel(
    private val getRecentEntries: GetRecentEntriesUseCase,
    private val searchEntries: SearchEntriesUseCase,
    private val deleteEntryUseCase: DeleteEntryUseCase
) : ViewModel() {

    data class UiState(
        val entries: List<FaultEntry> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun loadEntries(filter: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val entries: List<FaultEntry> = when (filter) {
                    "recent" -> getRecentEntries().getOrThrow()
                    "search" -> searchEntries(null, null, null).getOrThrow()
                    else -> emptyList()
                }

                _uiState.value = _uiState.value.copy(
                    entries = entries,
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load entries"
                )
            }
        }
    }


    fun deleteEntry(entry: FaultEntry) {
        viewModelScope.launch {
            deleteEntryUseCase(entry)
            _uiState.value = _uiState.value.copy(
                entries = _uiState.value.entries.filterNot { it.id == entry.id }
            )
        }
    }
}

