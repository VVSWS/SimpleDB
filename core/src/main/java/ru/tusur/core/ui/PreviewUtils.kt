package ru.tusur.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.tusur.core.ui.theme.CarFaultTheme
import ru.tusur.core.ui.theme.ThemeMode

@Preview(showBackground = true, name = "Light")
@Composable
fun LightPreview(content: @Composable () -> Unit) {
    CarFaultTheme(themeMode = ThemeMode.LIGHT) {
        content()
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
fun DarkPreview(content: @Composable () -> Unit) {
    CarFaultTheme(themeMode = ThemeMode.DARK) {
        content()
    }
}
