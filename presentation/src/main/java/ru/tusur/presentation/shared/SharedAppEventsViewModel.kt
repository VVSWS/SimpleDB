package ru.tusur.presentation.shared

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow


class SharedAppEventsViewModel : ViewModel() {

    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AppEvent> = _events

    fun emit(event: AppEvent) {
        _events.tryEmit(event)
    }
}

sealed interface AppEvent {
    data object DatabaseCreated : AppEvent
    data object DatabaseDeleted : AppEvent
    data object DatabaseMerged : AppEvent
}
