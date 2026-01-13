package ru.tusur.presentation.shared

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class SharedAppEventsViewModel : ViewModel() {

    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AppEvent> = _events

    suspend fun emit(event: AppEvent) {
        _events.emit(event)   // suspending, reliable
    }
}

sealed interface AppEvent {
    data object DatabaseCreated : AppEvent
    data object DatabaseMerged : AppEvent
    data object EntryChanged : AppEvent
}
