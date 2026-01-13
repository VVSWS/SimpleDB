package ru.tusur.presentation.mainscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.tusur.data.usecase.GetCurrentDatabaseInfoUseCase
import ru.tusur.domain.repository.DatabaseMaintenanceRepository
import ru.tusur.domain.repository.FaultRepository
import ru.tusur.presentation.shared.SharedAppEventsViewModel
import ru.tusur.presentation.shared.AppEvent


data class MainUiState(
    val dbName: String = "",
    val entryCount: Int = 0,
    val dbSizeBytes: Long = 0,
    val imageCount: Int = 0,
    val imagesFolderSizeBytes: Long = 0,
    val resetCompleted: Boolean = false
)

class MainViewModel(
    private val getCurrentDbInfo: GetCurrentDatabaseInfoUseCase,
    private val dbMaintenanceRepository: DatabaseMaintenanceRepository,
    private val faultRepository: FaultRepository,
    private val sharedEvents: SharedAppEventsViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshDbInfo()
        observeEvents()
    }

    /**
     * Fetch a fresh snapshot of DB info.
     */
    private fun refreshDbInfo() {
        viewModelScope.launch {
            val info = getCurrentDbInfo()
            _uiState.update {
                it.copy(
                    dbName = info.filename ?: "",
                    entryCount = info.entryCount,
                    dbSizeBytes = info.dbSizeBytes,
                    imageCount = info.imageCount,
                    imagesFolderSizeBytes = info.imagesFolderSizeBytes
                )
            }
        }
    }

    /**
     * React to global app events and refresh DB info.
     */
    private fun observeEvents() {
        viewModelScope.launch {
            sharedEvents.events.collect { event ->
                when (event) {
                    is AppEvent.DatabaseCreated,
                    is AppEvent.DatabaseMerged,
                    is AppEvent.EntryChanged -> refreshDbInfo()
                    else -> {}
                }
            }
        }
    }


    fun resetDatabase() {
        viewModelScope.launch {
            try {
                // 1. Delete all entries + delete all image files (your existing implementation)
                faultRepository.resetDatabase()


                // 2. Shrink the database file
                dbMaintenanceRepository.vacuum()


                // 3. Notify other screens
                sharedEvents.emit(AppEvent.EntryChanged)

                // 4. Update UI state
                _uiState.update { it.copy(resetCompleted = true) }

                // 5. Refresh DB info card (size will shrink after VACUUM)
                refreshDbInfo()

            } catch (e: Exception) {
                println("Reset failed: ${e.message}")

            }
        }
    }

}
