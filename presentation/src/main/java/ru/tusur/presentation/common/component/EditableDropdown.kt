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

// ---------------------------------------------------------
// Редактируемый выпадающий список (Dropdown)
// ---------------------------------------------------------
// Поддерживает:
// - Выбор элемента из списка
// - Добавление нового элемента (через диалог)
// - Удаление элемента (опционально)
// - Отображение ошибки
// - Опцию "Ничего не выбрано"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EditableDropdown(
    items: List<T>,                         // Список доступных элементов
    selectedItem: T?,                       // Выбранный элемент (может быть null)
    itemToString: (T) -> String,            // Функция преобразования элемента в строку
    onItemSelected: (T?) -> Unit,           // Callback при выборе элемента
    onAddNewItem: (String) -> Unit,         // Callback при добавлении нового элемента
    onDeleteItem: ((T) -> Unit)? = null,    // Callback при удалении (опционально)
    errorMessage: String? = null,           // Сообщение об ошибке
    showAddNewOption: Boolean = true,       // Показывать опцию "Добавить новый"
    placeholder: String? = null,            // Плейсхолдер (текст по умолчанию)
    modifier: Modifier = Modifier
) {
    // Состояния UI
    var expanded by remember { mutableStateOf(false) }          // Раскрыт ли список
    var showAddDialog by remember { mutableStateOf(false) }     // Показывать диалог добавления
    var newItemText by remember { mutableStateOf("") }          // Текст нового элемента
    var pendingDeleteItem by remember { mutableStateOf<T?>(null) }  // Элемент для удаления

    Column(modifier = modifier) {
        // ---------------------------------------------------------
        // Основной выпадающий список
        // ---------------------------------------------------------
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                // Поле ввода (только для чтения, отображает выбранное значение)
                OutlinedTextField(
                    value = selectedItem?.let { itemToString(it) }
                        ?: placeholder.orEmpty(),
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

                // Меню выпадающего списка
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Опция "Добавить новый"
                    if (showAddNewOption) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.dropdown_add_new)) },
                            onClick = {
                                expanded = false
                                showAddDialog = true
                            }
                        )
                    }

                    // Опция "Ничего не выбрано"
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.dropdown_none)) },
                        onClick = {
                            onItemSelected(null)
                            expanded = false
                        }
                    )

                    // Элементы списка
                    items.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(itemToString(item)) },
                            trailingIcon = {
                                // Кнопка удаления (если разрешено)
                                if (onDeleteItem != null) {
                                    IconButton(onClick = {
                                        pendingDeleteItem = item
                                        expanded = false
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.dialog_delete_title)
                                        )
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

        // ---------------------------------------------------------
        // Отображение сообщения об ошибке
        // ---------------------------------------------------------
        if (errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // ---------------------------------------------------------
        // Диалог добавления нового элемента
        // ---------------------------------------------------------
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

        // ---------------------------------------------------------
        // Диалог подтверждения удаления элемента
        // ---------------------------------------------------------
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