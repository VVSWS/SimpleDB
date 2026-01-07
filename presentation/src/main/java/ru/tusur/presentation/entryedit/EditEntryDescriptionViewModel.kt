package ru.tusur.presentation.entryedit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.usecase.entry.*
import ru.tusur.presentation.common.DescriptionError
import ru.tusur.core.files.ImageStorage


class EditEntryDescriptionViewModel(
    private val getEntryById: GetEntryByIdUseCase,
    private val createEntry: CreateEntryUseCase,
    private val updateEntry: UpdateEntryUseCase,
    private val deleteEntry: DeleteEntryUseCase,
    private val deleteImageUseCase: DeleteImageUseCase? = null // optional if you have it
) : ViewModel() {

    data class UiState(
        val entry: FaultEntry = FaultEntry(),
        val isEditMode: Boolean = false,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val saveCompleted: Boolean = false,
        val descriptionError: DescriptionError? = null
    ) {
        val isValid: Boolean
            get() = descriptionError == null && entry.description.isNotBlank()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // ---------------------------------------------------------
    // Load or initialize entry
    // ---------------------------------------------------------

    fun loadEntry(entryId: Long?) {
        if (entryId == null || entryId == 0L) {
            _uiState.value = UiState(
                entry = FaultEntry(),
                isEditMode = false,
                isLoading = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            getEntryById(entryId)
                .onSuccess { entry ->
                    _uiState.value = _uiState.value.copy(
                        entry = entry ?: FaultEntry(),
                        isEditMode = entry != null,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = UiState()
                }
        }
    }

    fun initializeNewEntry(entry: FaultEntry) {
        _uiState.value = _uiState.value.copy(entry = entry)
    }

    // ---------------------------------------------------------
    // Field updates
    // ---------------------------------------------------------

    fun onDescriptionChanged(description: String) {
        val error = if (description.isBlank()) DescriptionError.Empty else null

        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(description = description),
            descriptionError = error
        )
    }

    fun onImagesSelected(context: Context, uris: List<Uri>) {
        val newPaths = uris.map { ImageStorage.savePickedImage(context, it) }

        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(
                imageUris = _uiState.value.entry.imageUris + newPaths
            )
        )
    }

    // ---------------------------------------------------------
    // Remove image
    // ---------------------------------------------------------

    fun removeImage(path: String) {
        viewModelScope.launch {
            try {
                deleteImageUseCase?.invoke(path)

                val updated = _uiState.value.entry.copy(
                    imageUris = _uiState.value.entry.imageUris - path
                )

                _uiState.value = _uiState.value.copy(entry = updated)

            } catch (e: Exception) {
                // If you want error handling, add error to UiState
            }
        }
    }

    // ---------------------------------------------------------
    // Save / Delete
    // ---------------------------------------------------------

    fun saveEntry() {
        val state = _uiState.value
        if (!state.isValid) return

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                if (state.isEditMode) {
                    updateEntry(state.entry)
                } else {
                    createEntry(state.entry.copy(timestamp = System.currentTimeMillis()))
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

    fun deleteEntry() {
        if (!_uiState.value.isEditMode) return

        viewModelScope.launch {
            deleteEntry(_uiState.value.entry)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(saveCompleted = true)
                }
        }
    }

    fun consumeSaveCompleted() {
        _uiState.value = _uiState.value.copy(saveCompleted = false)
    }
}
