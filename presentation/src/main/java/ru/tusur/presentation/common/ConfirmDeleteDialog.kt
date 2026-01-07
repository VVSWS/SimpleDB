package ru.tusur.presentation.common

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ru.tusur.presentation.R
import ru.tusur.presentation.localization.LocalAppLanguage

@Composable
fun ConfirmDeleteDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val appLanguage = LocalAppLanguage.current
    val context = LocalContext.current

    key(appLanguage.locale, context) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.dialog_delete_confirm)) },
            text = {
                Text(stringResource(R.string.dialog_delete_message, itemName))
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.dialog_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dialog_delete_cancel))
                }
            }
        )
    }
}

