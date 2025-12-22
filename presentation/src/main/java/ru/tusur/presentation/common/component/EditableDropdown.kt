package ru.tusur.presentation.common.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun <T> EditableDropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    itemToString: (T) -> String,
    onItemSelected: (T) -> Unit,
    onAddNewItem: (String) -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(TextFieldValue(selectedItem?.let(itemToString) ?: "")) }

    Column(modifier) {

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    expanded = true
                },
                label = { Text(label) },
                isError = errorMessage != null,
                modifier = Modifier
                    .menuAnchor()   // ✔ Stable, correct for your BOM
                    .fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val filtered = items.filter {
                    itemToString(it).contains(text.text, ignoreCase = true)
                }

                filtered.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemToString(item)) },
                        onClick = {
                            text = TextFieldValue(itemToString(item))
                            expanded = false
                            onItemSelected(item)
                        }
                    )
                }

                DropdownMenuItem(
                    text = { Text("Add \"${text.text}\"") },
                    onClick = {
                        expanded = false
                        onAddNewItem(text.text)
                    }
                )
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
