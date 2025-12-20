package ru.tusur.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.tusur.stop.core.ui.theme.CarFaultTheme

@Preview(showBackground = true, name = "Light")
@Composable
fun LightPreview(content: @Composable () -> Unit) {
    CarFaultTheme(darkTheme = false) {
        content()
    }
}

@Preview(showBackground = true, name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DarkPreview(content: @Composable () -> Unit) {
    CarFaultTheme(darkTheme = true) {
        content()
    }
}