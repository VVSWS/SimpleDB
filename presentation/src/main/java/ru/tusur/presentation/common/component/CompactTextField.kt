package ru.tusur.presentation.common.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// ---------------------------------------------------------
// Компактное текстовое поле с отдельной меткой
// ---------------------------------------------------------
// Отличается от стандартного OutlinedTextField тем, что
// метка находится СВЕРХУ, а НЕ внутри поля ввода
// Используется для форм с ограниченным пространством
@Composable
fun CompactTextField(
    label: String,                          // Текст метки (над полем)
    value: String,                         // Текущее значение
    onValueChange: (String) -> Unit,       // Callback при изменении текста
    isError: Boolean = false,              // Флаг ошибки (подсветка красным)
    errorMessage: String? = null           // Сообщение об ошибке (отображается снизу)
) {
    Column {
        // ---------------------------------------------------------
        // Метка над полем ввода
        // ---------------------------------------------------------
        // Используется стиль labelLarge для хорошей читаемости
        Text(label, style = MaterialTheme.typography.labelLarge)

        // ---------------------------------------------------------
        // Поле ввода
        // ---------------------------------------------------------
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),   // Растягивание на всю ширину
            singleLine = true,                   // Одна строка (без переноса)
            isError = isError,                   // Подсветка границы при ошибке
            shape = MaterialTheme.shapes.small,  // Скругление 8dp (из AppShapes)
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary
            )
        )

        // ---------------------------------------------------------
        // Сообщение об ошибке (отображается только при ошибке)
        // ---------------------------------------------------------
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}