package ru.tusur.presentation.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.tusur.domain.usecase.database.GetCurrentDatabaseInfoUseCase

class MainViewModel(
    getCurrentDbInfo: GetCurrentDatabaseInfoUseCase
) : ViewModel() {

    data class UiState(
        val isActive: Boolean = false,
        val filename: String? = null,
        val entryCount: Int = 0
    )

    val uiState: StateFlow<UiState> = getCurrentDbInfo()
        .map { dbInfo ->
            UiState(
                isActive = dbInfo.isActive,
                filename = dbInfo.filename,
                entryCount = dbInfo.entryCount
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState()
        )
}