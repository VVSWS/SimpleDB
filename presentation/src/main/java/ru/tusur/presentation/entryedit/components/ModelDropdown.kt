package ru.tusur.presentation.entryedit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ru.tusur.domain.model.Model

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelDropdown(
    selectedModel: Model?,                     // nullable now
    models: List<Model>,                       // dynamic list from ViewModel
    newModelInput: String,                     // text for adding new model
    onModelSelected: (Model) -> Unit,
    onNewModelInputChanged: (String) -> Unit,
    onAddNewModel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedModel?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Model") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Existing models
            models.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model.name) },
                    onClick = {
                        onModelSelected(model)
                        expanded = false
                    }
                )
            }

            // Divider + Add new model
            HorizontalDivider()

            // Input field for new model
            DropdownMenuItem(
                text = {
                    OutlinedTextField(
                        value = newModelInput,
                        onValueChange = onNewModelInputChanged,
                        label = { Text("Add new model") }
                    )
                },
                onClick = { /* no-op */ }
            )

            // Confirm button
            DropdownMenuItem(
                text = { Text("Add") },
                onClick = {
                    onAddNewModel()
                    expanded = false
                }
            )
        }
    }
}
