package ru.tusur.presentation.common.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    itemToString: (T) -> String,
    onItemSelected: (T) -> Unit,
    onAddNewItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(selectedItem?.let(itemToString) ?: "") }

    Column(modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                expanded = true
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemToString(item)) },
                    onClick = {
                        text = itemToString(item)
                        expanded = false
                        onItemSelected(item)
                    }
                )
            }

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
