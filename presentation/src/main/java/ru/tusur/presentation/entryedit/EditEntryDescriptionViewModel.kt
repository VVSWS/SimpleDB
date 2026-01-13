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

class EditEntryDescriptionViewModel(
    private val id: Long,
    private val getEntryById: GetEntryByIdUseCase,
    private val createEntry: CreateEntryUseCase,
    private val updateEntry: UpdateEntryUseCase,
    private val deleteEntryUseCase: DeleteEntryUseCase,
    private val sharedEvents: SharedAppEventsViewModel        // NEW
) : ViewModel() {

    data class UiState(
        val entry: FaultEntry? = null,
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

    init {
        loadEntry()
    }

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
                _uiState.value.copy(
                    isLoading = false,
                    descriptionError = DescriptionError.Empty
                )
            }
        }
    }

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

    fun removeImage(context: Context, path: String) {
        val current = _uiState.value.entry ?: return

        // 1. Delete the actual file
        ImageStorage.deleteImageFile(context, path)

        // 2. Update UI state
        _uiState.value = _uiState.value.copy(
            entry = current.copy(
                imageUris = current.imageUris - path
            )
        )
    }


    // ---------------------------------------------------------
    // Save entry (update)
    // ---------------------------------------------------------

    fun saveEntry() {
        val state = _uiState.value
        val entry = state.entry ?: return
        if (!state.isValid) return

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                updateEntry(entry)

                // Notify main screen
                sharedEvents.emit(AppEvent.EntryChanged)

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
            try {
                deleteEntryUseCase(entry)

                // Notify main screen
                sharedEvents.emit(AppEvent.EntryChanged)

                _uiState.value = _uiState.value.copy(saveCompleted = true)
            } catch (_: Exception) {
                // Optional: error handling
            }
        }
    }

    fun consumeSaveCompleted() {
        _uiState.value = _uiState.value.copy(saveCompleted = false)
    }
}
