package ru.tusur.presentation.common.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> EditableDropdownSelector(
    items: List<T>,
    selectedItem: T?,
    itemToString: (T) -> String,
    onItemSelected: (T?) -> Unit,
    onAddNewItem: (String) -> Unit,
    onDeleteItem: ((T) -> Unit)? = null,
    errorMessage: String? = null,
    placeholder: String? = null,
    showAddNewOption: Boolean = true
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
            modifier = Modifier.fillMaxWidth()
        )
    }
}
