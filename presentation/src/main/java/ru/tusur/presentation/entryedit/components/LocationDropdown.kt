package ru.tusur.presentation.entryedit.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import ru.tusur.domain.model.Location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropdown(
    selected: Location,
    onSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val locations = listOf(
        Location("Engine"),
        Location("Transmission"),
        Location("Suspension"),
        Location("Brakes"),
        Location("Electrical"),
        Location("Body"),
        Location("Interior"),
        Location("Cooling System")
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
            label = { Text("Location") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            locations.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location.name) },
                    onClick = {
                        onSelected(location)
                        expanded = false
                    }
                )
            }
        }
    }
}
