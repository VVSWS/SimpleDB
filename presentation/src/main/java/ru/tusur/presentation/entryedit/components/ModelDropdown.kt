package ru.tusur.presentation.entryedit.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import ru.tusur.domain.model.Model

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelDropdown(
    selected: Model,
    onSelected: (Model) -> Unit,
    modifier: Modifier = Modifier
) {
    val models = listOf(
        Model("Toyota"),
        Model("Honda"),
        Model("BMW"),
        Model("Mercedes"),
        Model("Audi"),
        Model("Volkswagen"),
        Model("Ford"),
        Model("Nissan")
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Model") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            models.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model.name) },
                    onClick = {
                        onSelected(model)
                        expanded = false
                    }
                )
            }
        }
    }
}
