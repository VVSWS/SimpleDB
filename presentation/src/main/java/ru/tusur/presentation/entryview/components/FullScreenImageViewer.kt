package ru.tusur.presentation.entryview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.core.net.toUri

// ---------------------------------------------------------
// Компонент для просмотра изображения в полноэкранном режиме с зумом
// ---------------------------------------------------------
// Поддерживает жесты:
// - Одиночное нажатие: закрытие просмотрщика
// - Двойное нажатие: переключение масштаба (1x ↔ 2.5x)
// - Перетаскивание двумя пальцами: масштабирование и панорамирование
@Composable
fun ZoomableImage(
    uri: String,           // URI изображения (поддерживает content://, file://, абсолютный и относительный пути)
    onDismiss: () -> Unit // Callback для закрытия просмотрщика
) {
    // ---------------------------------------------------------
    // Состояния для масштаба и смещения (панорамирования)
    // ---------------------------------------------------------
    var scale by remember { mutableStateOf(1f) }           // Текущий масштаб (1x - 5x)
    var offset by remember { mutableStateOf(Offset.Zero) } // Смещение при панорамировании

    val context = LocalContext.current

    // ---------------------------------------------------------
    // Преобразование строки URI в модель, понятную Coil
    // ---------------------------------------------------------
    val model = remember(uri) {
        when {
            uri.startsWith("content://") -> uri.toUri()           // content:// URI (из галереи)
            uri.startsWith("file://") -> uri.toUri()              // file:// URI
            uri.startsWith("/") -> File(uri)                      // Абсолютный путь
            else -> File(context.filesDir, uri)                   // Относительный путь (внутри filesDir)
        }
    }

    // ---------------------------------------------------------
    // Полноэкранный контейнер с чёрным фоном
    // ---------------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // ---------------------------------------------------------
            // Жест: масштабирование и панорамирование (двумя пальцами)
            // ---------------------------------------------------------
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Обновление масштаба с ограничением от 1x до 5x
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    // Обновление смещения (панорамирование)
                    offset += pan
                }
            }
            // ---------------------------------------------------------
            // Жесты: двойное нажатие и одиночное нажатие
            // ---------------------------------------------------------
            .pointerInput(Unit) {
                detectTapGestures(
                    // Двойное нажатие: сброс или увеличение масштаба
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2.5f  // Переключение между 1x и 2.5x
                        offset = Offset.Zero                   // Сброс смещения при смене масштаба
                    },
                    // Одиночное нажатие: закрытие просмотрщика
                    onTap = { onDismiss() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // ---------------------------------------------------------
        // Изображение с применёнными трансформациями (масштаб + смещение)
        // ---------------------------------------------------------
        AsyncImage(
            model = model,
            contentDescription = null,  // Декоративное изображение, контентное описание не требуется
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,          // Масштаб по горизонтали
                    scaleY = scale,          // Масштаб по вертикали
                    translationX = offset.x, // Смещение по горизонтали
                    translationY = offset.y  // Смещение по вертикали
                )
                .fillMaxSize(),
            contentScale = ContentScale.Fit  // Вписывание изображения в доступную область
        )
    }
}