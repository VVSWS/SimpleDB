package ru.tusur.presentation.entryedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.usecase.entry.*
import ru.tusur.domain.model.Year
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Location


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

    /**
     * Load entry for editing.
     * If entryId == null → new entry mode.
     */
    fun loadEntry(entryId: Long?) {
        if (entryId == null) {
            // NEW ENTRY MODE
            _uiState.value = UiState(
                entry = FaultEntry(),
                isEditMode = false,
                isLoading = false
            )
            return
        }

        // EDIT MODE
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            getEntryById(entryId)
                .onSuccess { entry ->
                    _uiState.value = UiState(
                        entry = entry ?: FaultEntry(),
                        isEditMode = entry != null,
                        isLoading = false
                    )
                }
                .onFailure {
                    // If loading fails → fallback to new entry mode
                    _uiState.value = UiState(
                        entry = FaultEntry(),
                        isEditMode = false,
                        isLoading = false
                    )
                }
        }
    }

    /**
     * Update description field.
     */
    fun onDescriptionChanged(description: String) {
        val error = if (description.isBlank()) {
            "Description cannot be empty"
        } else null

        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(description = description),
            descriptionError = error
        )
    }

    /**
     * Save entry (create or update).
     */
    fun saveEntry() {
        val state = _uiState.value

        if (!state.isValid) return

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                if (state.isEditMode) {
                    // UPDATE EXISTING ENTRY
                    updateEntry(state.entry)
                } else {
                    // CREATE NEW ENTRY
                    val newEntry = state.entry.copy(
                        timestamp = System.currentTimeMillis()
                    )
                    createEntry(newEntry)
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveCompleted = true
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    /**
     * Delete entry (only in edit mode).
     */
    fun deleteEntry() {
        if (!_uiState.value.isEditMode) return

        viewModelScope.launch {
            deleteEntry(_uiState.value.entry)
                .onSuccess {
                    // Navigation handled in screen
                    _uiState.value = _uiState.value.copy(saveCompleted = true)
                }
        }
    }

    /**
     * Reset saveCompleted flag after navigation.
     */
    fun consumeSaveCompleted() {
        _uiState.value = _uiState.value.copy(saveCompleted = false)
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(title = title)
        )
    }

    fun onYearChanged(year: Year) {
        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(year = year)
        )
    }

    fun onModelChanged(model: Model) {
        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(model = model)
        )
    }

    fun onLocationChanged(location: Location) {
        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(location = location)
        )
    }


}
