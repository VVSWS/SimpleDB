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
    private val deleteEntryUseCase: DeleteEntryUseCase,
    private val deleteImageUseCase: DeleteImageUseCase? = null
) : ViewModel() {

    data class UiState(
        val entry: FaultEntry? = null,
        val isEditMode: Boolean = false,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val saveCompleted: Boolean = false,
        val descriptionError: DescriptionError? = null
    ) {
        val isValid: Boolean
            get() = entry != null &&
                    descriptionError == null &&
                    entry.description.isNotBlank()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // ---------------------------------------------------------
    // Load entry
    // ---------------------------------------------------------

    fun loadEntry(entryId: Long) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            getEntryById(entryId)
                .onSuccess { entry ->
                    _uiState.value = _uiState.value.copy(
                        entry = entry,
                        isEditMode = true,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = UiState(isLoading = false)
                }
        }
    }

    // ---------------------------------------------------------
    // Field updates
    // ---------------------------------------------------------

    fun onDescriptionChanged(description: String) {
        val current = _uiState.value.entry ?: return

        val error = if (description.isBlank()) DescriptionError.Empty else null

        _uiState.value = _uiState.value.copy(
            entry = current.copy(description = description),
            descriptionError = error
        )
    }

    fun onImagesSelected(context: Context, uris: List<Uri>) {
        val current = _uiState.value.entry ?: return

        val newPaths = uris.map { ImageStorage.savePickedImage(context, it) }

        _uiState.value = _uiState.value.copy(
            entry = current.copy(
                imageUris = current.imageUris + newPaths
            )
        )
    }

    // ---------------------------------------------------------
    // Remove image (UI only, DB updated on save)
    // ---------------------------------------------------------

    fun removeImage(path: String) {
        val current = _uiState.value.entry ?: return

        _uiState.value = _uiState.value.copy(
            entry = current.copy(
                imageUris = current.imageUris - path
            )
        )
    }

    // ---------------------------------------------------------
    // Save entry
    // ---------------------------------------------------------

    fun saveEntry() {
        val state = _uiState.value
        val entry = state.entry ?: return
        if (!state.isValid) return

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                updateEntry(entry)

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveCompleted = true
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    // ---------------------------------------------------------
    // Delete entry
    // ---------------------------------------------------------

    fun deleteEntry() {
        val entry = _uiState.value.entry ?: return

        viewModelScope.launch {
            deleteEntryUseCase(entry)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(saveCompleted = true)
                }
        }
    }

    fun consumeSaveCompleted() {
        _uiState.value = _uiState.value.copy(saveCompleted = false)
    }
}
