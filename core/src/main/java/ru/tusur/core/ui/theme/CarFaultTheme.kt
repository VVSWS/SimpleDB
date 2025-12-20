package ru.tusur.core.ui.theme

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration

// ✅ Поддержка динамических строк с переключением языка
@Composable
@ReadOnlyComposable
fun stringResource(@StringRes id: Int): String {
    return stringResource(id = id)
}

// ✅ Автоопределение темы
@Composable
@ReadOnlyComposable
fun isSystemInDarkTheme(): Boolean {
    return LocalConfiguration.current.uiMode and UI_MODE_NIGHT_YES != 0
}