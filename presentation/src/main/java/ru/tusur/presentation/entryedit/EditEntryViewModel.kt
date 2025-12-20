package ru.tusur.presentation.entryedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.tusur.stop.core.model.Failure
import ru.tusur.stop.domain.model.FaultEntry
import ru.tusur.stop.domain.usecase.entry.*

class EditEntryViewModel(
    private val getEntryById: GetEntryByIdUseCase,
    private val createEntry: CreateEntryUseCase,
    private val updateEntry: UpdateEntryUseCase,
    private val deleteEntry: DeleteEntryUseCase
) : ViewModel() {

    data class UiState(
        val entry: FaultEntry = FaultEntry(),
        val isEditMode: Boolean = false,
        val isLoading: Boolean = false,
        val error: Failure? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun loadEntry(entryId: Long?) {
        if (entryId == null) return

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            getEntryById(entryId).onSuccess { entry ->
                _uiState.value = UiState(
                    entry = entry ?: FaultEntry(),
                    isEditMode = entry != null
                )
            }.onFailure { error ->
                _uiState.value = UiState(error = Failure.from(error))
            }
        }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(description = description)
        )
    }

    fun saveEntry() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val entry = _uiState.value.entry
            val result = if (_uiState.value.isEditMode) {
                updateEntry(entry)
            } else {
                createEntry(entry.copy(timestamp = System.currentTimeMillis() / 1000))
            }

            result.onSuccess {
                // TODO: navigate back
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = Failure.from(error)
                )
            }
        }
    }

    fun deleteEntry() {
        if (!_uiState.value.isEditMode) return

        viewModelScope.launch {
            deleteEntry(_uiState.value.entry).onSuccess {
                // TODO: navigate back
            }
        }
    }
}