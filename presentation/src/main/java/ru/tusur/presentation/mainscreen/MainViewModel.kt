package ru.tusur.presentation.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.tusur.data.usecase.GetCurrentDatabaseInfoUseCase
import ru.tusur.presentation.shared.SharedAppEventsViewModel
import ru.tusur.presentation.shared.AppEvent

data class MainUiState(
    val dbName: String = "",
    val entryCount: Int = 0
)

class MainViewModel(
    private val getCurrentDbInfo: GetCurrentDatabaseInfoUseCase,
    private val sharedEvents: SharedAppEventsViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeDbInfo()
        observeEvents()
    }

    private fun observeDbInfo() {
        viewModelScope.launch {
            getCurrentDbInfo().collectLatest { info ->
                _uiState.update {
                    it.copy(
                        dbName = info.filename ?: "",
                        entryCount = info.entryCount
                    )
                }
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            sharedEvents.events.collect { event ->
                when (event) {
                    is AppEvent.DatabaseCreated,
                    is AppEvent.DatabaseDeleted,
                    is AppEvent.DatabaseMerged -> observeDbInfo()
                }
            }
        }
    }
}
