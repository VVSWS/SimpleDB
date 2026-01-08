package ru.tusur.presentation.entryview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

class RecordingViewViewModel(
    private val repository: FaultRepository,
    private val entryId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(RecordingViewUiState())
    val state: StateFlow<RecordingViewUiState> = _state

    init {
        loadEntry()
    }

    private fun loadEntry() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val entry = repository.getEntryWithRecording(entryId)
                _state.value = RecordingViewUiState(entry = entry)
            } catch (e: Exception) {
                _state.value = RecordingViewUiState(error = e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() = loadEntry()

    // ---------------------------------------------------------
    // Delete image by URI (String)
    // ---------------------------------------------------------
    fun deleteImage(uri: String) {
        viewModelScope.launch {
            try {
                repository.deleteImage(uri)

                // Reload entry so UI updates immediately
                val updated = repository.getEntryWithRecording(entryId)
                _state.value = _state.value.copy(entry = updated)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to delete image"
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Update description using updateEntry(entry)
    // ---------------------------------------------------------
    fun updateDescription(newDescription: String) {
        val current = _state.value.entry ?: return

        val updatedEntry = FaultEntry(
            id = current.id,
            title = current.title,
            year = current.year,
            brand = current.brand,
            model = current.model,
            location = current.location,
            timestamp = current.timestamp,
            description = newDescription,
            imageUris = current.imageUris
        )

        viewModelScope.launch {
            try {
                repository.updateEntry(updatedEntry)

                // Reload entry so UI updates immediately
                val refreshed = repository.getEntryWithRecording(entryId)
                _state.value = _state.value.copy(entry = refreshed)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to update description"
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Delete entire entry
    // ---------------------------------------------------------
    fun deleteEntry() {
        val e = _state.value.entry ?: return

        val faultEntry = FaultEntry(
            id = e.id,
            title = e.title,
            year = e.year,
            brand = e.brand,
            model = e.model,
            location = e.location,
            timestamp = e.timestamp,
            description = e.description ?: "",
            imageUris = e.imageUris
        )

        viewModelScope.launch {
            try {
                repository.deleteEntry(faultEntry)
                _state.value = _state.value.copy(isDeleted = true)
            } catch (ex: Exception) {
                _state.value = _state.value.copy(
                    error = ex.message ?: "Failed to delete entry"
                )
            }
        }
    }
}

data class RecordingViewUiState(
    val isLoading: Boolean = false,
    val entry: EntryWithRecording? = null,
    val error: String? = null,
    val isDeleted: Boolean = false
)
