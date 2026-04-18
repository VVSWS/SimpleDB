package ru.tusur.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.tusur.core.ui.theme.CarFaultTheme
import ru.tusur.core.ui.theme.ThemeMode

// ---------------------------------------------------------
// Утилиты для предпросмотра Compose-компонентов в Android Studio
// ---------------------------------------------------------
// Оборачивают компоненты в тему приложения для корректного отображения
// Позволяют визуально проверить компоненты в светлой и тёмной теме

// ---------------------------------------------------------
// Предпросмотр в светлой теме
// ---------------------------------------------------------
// Аннотация @Preview указывает Android Studio показывать этот компонент в окне предпросмотра
// showBackground = true - отображать фон за компонентом
// name = "Light" - название варианта предпросмотра в панели инструментов
@Preview(showBackground = true, name = "Light")
@Composable
fun LightPreview(content: @Composable () -> Unit) {
    // Оборачивание контента в тему со светлым режимом
    CarFaultTheme(themeMode = ThemeMode.LIGHT) {
        content()
    }
}

// ---------------------------------------------------------
// Предпросмотр в тёмной теме
// ---------------------------------------------------------
// Позволяет проверить читаемость текста и цветовую схему в тёмном режиме
// Автоматически инвертирует цвета компонентов в соответствии с Material You
@Preview(showBackground = true, name = "Dark")
@Composable
fun DarkPreview(content: @Composable () -> Unit) {
    // Оборачивание контента в тему с тёмным режимом
    CarFaultTheme(themeMode = ThemeMode.DARK) {
        content()
    }
}