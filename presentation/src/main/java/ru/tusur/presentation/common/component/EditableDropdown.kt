package ru.tusur.presentation.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tusur.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EditableDropdown(
    items: List<T>,
    selectedItem: T?,
    itemToString: (T) -> String,
    onItemSelected: (T?) -> Unit,
    onAddNewItem: (String) -> Unit,
    onDeleteItem: ((T) -> Unit)? = null,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newItemText by remember { mutableStateOf("") }

    // For delete confirmation
    var pendingDeleteItem by remember { mutableStateOf<T?>(null) }

    Column(modifier = modifier) {

        // Center the dropdown field + menu
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                OutlinedTextField(
                    value = selectedItem?.let { itemToString(it) } ?: stringResource(R.string.dropdown_none),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledIndicatorColor = MaterialTheme.colorScheme.primary,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = false
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.dropdown_add_new)) },
                        onClick = {
                            expanded = false
                            showAddDialog = true
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.dropdown_none)) },
                        onClick = {
                            onItemSelected(null)
                            expanded = false
                        }
                    )

                    items.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(itemToString(item)) },
                            trailingIcon = {
                                if (onDeleteItem != null) {
                                    IconButton(onClick = {
                                        pendingDeleteItem = item
                                        expanded = false
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                            },
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Error message
        if (errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Add new item dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(stringResource(R.string.dialog_add_new_title)) },
                text = {
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newItemText.isNotBlank()) {
                                onAddNewItem(newItemText)
                            }
                            newItemText = ""
                            showAddDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.dialog_add))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text(stringResource(R.string.dialog_cancel))
                    }
                }
            )
        }

        // Delete confirmation dialog
        if (pendingDeleteItem != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteItem = null },
                title = { Text(stringResource(R.string.dialog_delete_title)) },
                text = {
                    Text(
                        stringResource(
                            R.string.dialog_delete_message,
                            itemToString(pendingDeleteItem!!)
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteItem?.invoke(pendingDeleteItem!!)
                        pendingDeleteItem = null
                    }) {
                        Text(stringResource(R.string.dialog_delete_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteItem = null }) {
                        Text(stringResource(R.string.dialog_delete_cancel))
                    }
                }
            )
        }
    }
}
