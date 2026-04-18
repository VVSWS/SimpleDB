package ru.tusur.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------
// Модификатор для создания кликабельных элементов с кастомной рябью (ripple)
// ---------------------------------------------------------
// Расширение Modifier, добавляющее эффект пульсации при нажатии
// Использует composed для сохранения состояния при перекомпозиции
// Кастомизирует цвет ripple-эффекта (полупрозрачный серый)
fun Modifier.clickableRipple(
    enabled: Boolean = true,    // Включена ли интерактивность (можно отключить для неактивных элементов)
    onClick: () -> Unit         // Callback, вызываемый при нажатии
): Modifier = composed {
    // ---------------------------------------------------------
    // Создание модификатора clickable с кастомной индикацией
    // ---------------------------------------------------------
    clickable(
        // Источник взаимодействий (нажатия, фокус, ховер)
        // remember сохраняет экземпляр между перерисовками
        interactionSource = remember { MutableInteractionSource() },

        // Индикация нажатия - ripple с полупрозрачным серым цветом
        // Альфа 0.2 обеспечивает едва заметную, но различимую пульсацию
        indication = ripple(color = Color.Gray.copy(alpha = 0.2f)),

        // Флаг включения/отключения кликабельности
        enabled = enabled,

        // Обработчик нажатия
        onClick = onClick
    )
}