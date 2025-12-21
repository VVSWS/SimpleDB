package ru.tusur.presentation.entrylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.tusur.core.model.Failure
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.usecase.entry.GetRecentEntriesUseCase
import ru.tusur.domain.usecase.entry.SearchEntriesUseCase

class EntryListViewModel(
    private val getRecentEntries: GetRecentEntriesUseCase,
    private val searchEntries: SearchEntriesUseCase
) : ViewModel() {

    sealed class Filter {
        object Recent : Filter()
        data class Custom(
            val year: Int? = null,
            val model: String? = null,
            val location: String? = null
        ) : Filter()
    }

    data class UiState(
        val entries: List<FaultEntry> = emptyList(),
        val isLoading: Boolean = false,
        val error: Failure? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun loadEntries(filter: Filter) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = when (filter) {
                is Filter.Recent -> getRecentEntries(5)
                is Filter.Custom -> searchEntries(filter.year, filter.model, filter.location)
            }

            _uiState.value = when {
                result.isSuccess -> UiState(entries = result.getOrNull() ?: emptyList())
                else -> UiState(error = Failure.from(result.exceptionOrNull()!!))
            }
        }
    }
}