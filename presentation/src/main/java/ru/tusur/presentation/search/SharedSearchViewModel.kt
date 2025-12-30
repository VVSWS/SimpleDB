package ru.tusur.presentation.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.tusur.domain.model.SearchFilter

class SharedSearchViewModel : ViewModel() {

    private val _filter = MutableStateFlow(SearchFilter())
    val filter: StateFlow<SearchFilter> = _filter

    fun updateFilter(newFilter: SearchFilter) {
        _filter.value = newFilter
    }
}

