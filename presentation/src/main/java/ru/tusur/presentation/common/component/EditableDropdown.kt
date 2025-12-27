package ru.tusur.presentation.common.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EditableDropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    itemToString: (T) -> String,
    onItemSelected: (T) -> Unit,
    onAddNewItem: (String) -> Unit,
    allowFreeInput: Boolean = true,   // 🔥 NEW
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // 🔥 If free input allowed → keep local text state
    // 🔥 If not → derive from selectedItem
    var localText by remember { mutableStateOf("") }
    val text = if (allowFreeInput) localText else selectedItem?.let(itemToString) ?: ""

    Column(modifier) {

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { newText ->
                    expanded = true

                    if (allowFreeInput) {
                        localText = newText
                    }

                    // Auto-select if exact match
                    val match = items.firstOrNull {
                        itemToString(it).equals(newText, ignoreCase = true)
                    }
                    if (match != null) {
                        onItemSelected(match)
                    }
                },
                label = { Text(label) },
                isError = errorMessage != null,
                modifier = Modifier
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
                    .fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val filtered = items.filter {
                    itemToString(it).contains(text, ignoreCase = true)
                }

                filtered.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemToString(item)) },
                        onClick = {
                            expanded = false
                            onItemSelected(item)
                            if (allowFreeInput) {
                                localText = itemToString(item)
                            }
                        }
                    )
                }

                if (allowFreeInput) {
                    DropdownMenuItem(
                        text = { Text("Add \"$text\"") },
                        onClick = {
                            expanded = false
                            onAddNewItem(text)
                        }
                    )
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

