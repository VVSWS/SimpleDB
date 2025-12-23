package ru.tusur.presentation.entryedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.usecase.entry.*

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
        val isSaving: Boolean = false,
        val saveCompleted: Boolean = false,
        val descriptionError: String? = null
    ) {
        val isValid: Boolean
            get() = descriptionError == null && entry.description.isNotBlank()
    }


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
            }.onFailure {
                // No `error` field anymore → just reset state
                _uiState.value = UiState()
            }
        }
    }


    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(description = description)
        )
    }

    fun saveEntry() {
        val state = _uiState.value

        // Basic validation guard
        if (!state.isValid) return

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                if (state.isEditMode) {
                    // Existing entry -> update
                    updateEntry(state.entry)
                } else {
                    // New entry -> create with timestamp if needed
                    val newEntry = state.entry.copy(
                        // adjust field name if different

                    )
                    createEntry(newEntry)
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveCompleted = true
                )
            } catch (e: Exception) {
                // For now just stop saving; we’ll improve error reporting later
                _uiState.value = _uiState.value.copy(isSaving = false)
                // Log.e("EditEntryViewModel", "Error saving entry", e)
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
    fun consumeSaveCompleted() {
        _uiState.value = _uiState.value.copy(saveCompleted = false)
    }
}