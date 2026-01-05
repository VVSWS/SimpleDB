package ru.tusur.presentation.common.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> EditableDropdownSelector(
    label: String, // still passed in, but used only for errorMessage
    items: List<T>,
    selectedItem: T?,
    itemToString: (T) -> String,
    onItemSelected: (T?) -> Unit,
    onAddNewItem: (String) -> Unit,
    onDeleteItem: ((T) -> Unit)? = null,
    errorMessage: String? = null
) {
    Column {
        // ❌ Removed the top label completely

        EditableDropdown(
            items = items,
            selectedItem = selectedItem,
            itemToString = itemToString,
            onItemSelected = onItemSelected,
            onAddNewItem = onAddNewItem,
            onDeleteItem = onDeleteItem,
            errorMessage = errorMessage, // bottom label stays
            modifier = Modifier.fillMaxWidth()
        )
    }
}
