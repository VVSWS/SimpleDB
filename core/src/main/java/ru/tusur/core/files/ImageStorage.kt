package ru.tusur.core.files

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

// ---------------------------------------------------------
// Хранилище изображений (объект-синглтон)
// ---------------------------------------------------------
// Управляет сохранением, загрузкой и удалением изображений
// Все изображения хранятся в поддиректории "images" внутри filesDir приложения
object ImageStorage {

    // ---------------------------------------------------------
    // Имя директории для хранения изображений
    // ---------------------------------------------------------
    private const val IMAGES_DIR = "images"

    // ---------------------------------------------------------
    // Нормализация пути к изображению
    // ---------------------------------------------------------
    // Приводит различные форматы путей к единому виду:
    // - "images/filename.jpg" -> "images/filename.jpg"
    // - "filename.jpg" -> "images/filename.jpg"
    // - "file:///data/.../images/filename.jpg" -> "images/filename.jpg"
    // - "content://..." -> "images/filename.jpg"
    // - любой путь, содержащий "images/" -> извлекается имя файла
    private fun normalize(relativePath: String): String {
        // Очистка префиксов URI и извлечение имени файла после последнего вхождения "images/"
        val clean = relativePath
            .removePrefix("file://")
            .removePrefix("content://")
            .substringAfterLast("$IMAGES_DIR/")
        // Возврат нормализованного пути с указанием директории
        return "$IMAGES_DIR/$clean"
    }

    // ---------------------------------------------------------
    // Получение File-объекта по относительному пути
    // ---------------------------------------------------------
    // Преобразует любой формат пути в корректный File внутри filesDir
    // Используется для чтения, удаления или проверки существования файла
    fun resolveImageFile(context: Context, relativePath: String): File {
        // Нормализация пути к единому формату
        val cleanPath = normalize(relativePath)
        // Создание File внутри приватной директории приложения
        return File(context.filesDir, cleanPath)
    }

    // ---------------------------------------------------------
    // Сохранение выбранного изображения
    // ---------------------------------------------------------
    // Принимает URI изображения из системного пикера (Gallery/Camera)
    // Копирует файл в приватную директорию приложения
    // Возвращает нормализованный относительный путь для хранения в БД
    fun savePickedImage(context: Context, sourceUri: Uri): String {
        // Создание директории images, если она не существует
        val imagesDir = File(context.filesDir, IMAGES_DIR)
        if (!imagesDir.exists()) imagesDir.mkdirs()

        // Генерация уникального имени файла на основе временной метки
        // Формат: img_1734567890123.jpg
        val filename = "img_${System.currentTimeMillis()}.jpg"
        val file = File(imagesDir, filename)

        // Копирование содержимого из sourceUri в созданный файл
        context.contentResolver.openInputStream(sourceUri).use { input ->
            FileOutputStream(file).use { output ->
                // Побайтовое копирование из входного потока в выходной
                input?.copyTo(output)
            }
        }

        // Возврат относительного пути для сохранения в базе данных
        return "$IMAGES_DIR/$filename"
    }

    // ---------------------------------------------------------
    // Удаление файла изображения
    // ---------------------------------------------------------
    // Удаляет физический файл с диска
    // Работает даже если путь получен из UI или БД в разных форматах
    // Не вызывает ошибку, если файл не существует
    fun deleteImageFile(context: Context, relativePath: String) {
        // Получение File-объекта по любому формату пути
        val file = resolveImageFile(context, relativePath)
        // Удаление файла, если он существует
        if (file.exists()) file.delete()
    }

    // ---------------------------------------------------------
    // Очистка всех изображений
    // ---------------------------------------------------------
    // Полностью удаляет директорию images и создаёт её заново пустой
    // Используется при сбросе данных или переключении базы данных
    fun clearAllImages(filesDir: File) {
        // Создание ссылки на директорию images
        val imagesDir = File(filesDir, "images")
        // Если директория существует
        if (imagesDir.exists()) {
            // Рекурсивное удаление всех файлов и поддиректорий
            imagesDir.deleteRecursively()
            // Создание пустой директории заново
            imagesDir.mkdirs()
        }
    }
}