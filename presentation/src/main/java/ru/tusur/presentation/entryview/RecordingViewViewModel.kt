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
                _state.value = RecordingViewUiState(isLoading = true)
                val entry = repository.getEntryWithRecording(entryId)
                _state.value = RecordingViewUiState(entry = entry)
            } catch (e: Exception) {
                _state.value = RecordingViewUiState(error = e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() = loadEntry()


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
                _state.value = _state.value.copy(error = ex.message ?: "Failed to delete entry")
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
