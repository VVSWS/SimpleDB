package ru.tusur.presentation.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.tusur.domain.model.SearchFilter

// ---------------------------------------------------------
// Общая ViewModel для хранения состояния поиска между экранами
// ---------------------------------------------------------
// Позволяет обмениваться фильтрами поиска между экраном поиска (EntrySearchScreen)
// и экраном списка результатов (EntryListScreen)
// Живёт дольше отдельных экранов (область видимости - уровень приложения или навигации)
class SharedSearchViewModel : ViewModel() {

    // ---------------------------------------------------------
    // Текущий фильтр поиска
    // ---------------------------------------------------------
    // По умолчанию пустой фильтр (все поля null - без фильтрации)
    private val _filter = MutableStateFlow(SearchFilter())
    val filter: StateFlow<SearchFilter> = _filter

    // ---------------------------------------------------------
    // Обновление фильтра поиска
    // ---------------------------------------------------------
    // Вызывается из EntrySearchScreen при нажатии кнопки поиска
    // Значение будет прочитано в EntryListScreen для выполнения поиска
    fun updateFilter(newFilter: SearchFilter) {
        _filter.value = newFilter
    }
}