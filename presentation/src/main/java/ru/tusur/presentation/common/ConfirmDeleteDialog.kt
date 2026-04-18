package ru.tusur.presentation.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.tusur.presentation.R

// ---------------------------------------------------------
// Переиспользуемый диалог подтверждения удаления
// ---------------------------------------------------------
// Отображает стандартное окно с вопросом о подтверждении удаления элемента
// Используется на экранах списка записей и просмотра деталей
@Composable
fun ConfirmDeleteDialog(
    itemName: String,          // Название удаляемого элемента (для отображения в сообщении)
    onConfirm: () -> Unit,     // Callback при подтверждении удаления
    onDismiss: () -> Unit      // Callback при отмене или закрытии диалога
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Заголовок диалога: "Удаление"
            Text(stringResource(R.string.dialog_delete_title))
        },
        text = {
            // Сообщение: "Вы уверены, что хотите удалить \"{itemName}\"?"
            Text(stringResource(R.string.dialog_delete_message, itemName))
        },
        confirmButton = {
            // Кнопка подтверждения: "Удалить"
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.dialog_delete_confirm))
            }
        },
        dismissButton = {
            // Кнопка отмены: "Отмена"
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_delete_cancel))
            }
        }
    )
}