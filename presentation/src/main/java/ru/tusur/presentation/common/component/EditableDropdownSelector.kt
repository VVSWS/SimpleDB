package ru.tusur.presentation.common.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// ---------------------------------------------------------
// Обёртка над EditableDropdown для упрощённого использования
// ---------------------------------------------------------
// Предоставляет ту же функциональность, что и EditableDropdown,
// но с предустановленным модификатором fillMaxWidth и обёрткой в Column
// Используется для единообразного отображения выпадающих списков
// в формах создания и редактирования записей
@Composable
fun <T> EditableDropdownSelector(
    items: List<T>,                         // Список доступных элементов
    selectedItem: T?,                       // Выбранный элемент (может быть null)
    itemToString: (T) -> String,            // Функция преобразования элемента в строку
    onItemSelected: (T?) -> Unit,           // Callback при выборе элемента
    onAddNewItem: (String) -> Unit,         // Callback при добавлении нового элемента
    onDeleteItem: ((T) -> Unit)? = null,    // Callback при удалении (опционально)
    errorMessage: String? = null,           // Сообщение об ошибке
    placeholder: String? = null,            // Плейсхолдер (текст по умолчанию)
    showAddNewOption: Boolean = true        // Показывать опцию "Добавить новый"
) {
    Column {
        EditableDropdown(
            items = items,
            selectedItem = selectedItem,
            itemToString = itemToString,
            onItemSelected = onItemSelected,
            onAddNewItem = onAddNewItem,
            onDeleteItem = onDeleteItem,
            errorMessage = errorMessage,
            showAddNewOption = showAddNewOption,
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth()  // Растягивание на всю ширину родителя
        )
    }
}