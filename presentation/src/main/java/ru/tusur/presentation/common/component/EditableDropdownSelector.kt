package ru.tusur.presentation.common.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ru.tusur.presentation.localization.LocalAppLanguage

@Composable
fun <T> EditableDropdownSelector(
    items: List<T>,
    selectedItem: T?,
    itemToString: (T) -> String,
    onItemSelected: (T?) -> Unit,
    onAddNewItem: (String) -> Unit,
    onDeleteItem: ((T) -> Unit)? = null,
    errorMessage: String? = null
) {
    val appLanguage = LocalAppLanguage.current
    val context = LocalContext.current

    key(appLanguage.locale, context) {
        Column {
            EditableDropdown(
                items = items,
                selectedItem = selectedItem,
                itemToString = itemToString,
                onItemSelected = onItemSelected,
                onAddNewItem = onAddNewItem,
                onDeleteItem = onDeleteItem,
                errorMessage = errorMessage,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
