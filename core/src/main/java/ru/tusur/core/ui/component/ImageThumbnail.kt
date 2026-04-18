package ru.tusur.core.ui.component

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import java.io.File

// ---------------------------------------------------------
// Компонент миниатюры изображения
// ---------------------------------------------------------
// Отображает изображение в виде квадратной миниатюры 64x64 dp
// Поддерживает различные форматы URI: content://, file://, абсолютный путь, относительный путь
// При нажатии вызывает callback с URI изображения
@Composable
fun ImageThumbnail(
    uri: String,                                    // Путь к изображению (в любом формате)
    contentDescription: String = "Fault image",    // Описание для доступности (TalkBack)
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,  // Модификатор для кастомизации
    onClick: (String) -> Unit = {}                  // Callback при нажатии, передаёт uri
) {
    // ---------------------------------------------------------
    // Получение контекста через Compose
    // ---------------------------------------------------------
    // Нужен для доступа к filesDir при построении абсолютного пути
    val context = LocalContext.current

    // ---------------------------------------------------------
    // Преобразование строки URI в модель, понятную Coil
    // ---------------------------------------------------------
    // remember сохраняет результат между перерисовками
    // Пересоздаётся только при изменении uri
    val model = remember(uri) {
        when {
            // Обработка content:// URI (например, из галереи)
            uri.startsWith("content://") -> uri.toUri()

            // Обработка file:// URI
            uri.startsWith("file://") -> uri.toUri()

            // Обработка абсолютного пути, начинающегося с /
            uri.startsWith("/") -> File(uri)

            // Обработка относительного пути (например, "images/img_123.jpg")
            // Преобразование в полный путь внутри filesDir приложения
            else -> File(context.filesDir, uri)
        }
    }

    // ---------------------------------------------------------
    // Асинхронная загрузка и отображение изображения (Coil)
    // ---------------------------------------------------------
    // Поддерживает кэширование, масштабирование и placeholder'ы
    AsyncImage(
        model = model,                              // Источник изображения (File или Uri)
        contentDescription = contentDescription,    // Описание для доступности
        contentScale = ContentScale.Crop,           // Обрезка с сохранением пропорций (заполнение всей области)
        modifier = modifier
            .size(64.dp)                            // Фиксированный размер 64x64 dp
            .clip(RoundedCornerShape(8.dp))         // Скругление углов (8 dp радиус)
            .clickable { onClick(uri) }             // Обработчик нажатия
    )
}