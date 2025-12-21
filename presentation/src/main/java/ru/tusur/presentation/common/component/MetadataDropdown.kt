package ru.tusur.presentation.common.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> MetadataDropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemToString: (T) -> String,
    modifier: Modifier = Modifier,
    onAddNewItem: ((String) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var newItemText by remember { mutableStateOf("") }

    OutlinedTextField(
        value = selectedItem?.let(itemToString) ?: "",
        onValueChange = { },
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            // Кнопка "+" если поддерживается добавление
            if (onAddNewItem != null) {
                androidx.compose.material3.Button(
                    onClick = {
                        if (newItemText.isNotEmpty()) {
                            onAddNewItem(newItemText)
                            newItemText = ""
                        }
                    },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("+")
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = { Text(itemToString(item)) },
                onClick = {
                    onItemSelected(item)
                    expanded = false
                }
            )
        }
        if (onAddNewItem != null) {
            androidx.compose.material3.OutlinedTextField(
                value = newItemText,
                onValueChange = { newItemText = it },
                placeholder = { Text("New...") },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}