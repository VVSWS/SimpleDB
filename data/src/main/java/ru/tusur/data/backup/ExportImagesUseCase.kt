package ru.tusur.data.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.documentfile.provider.DocumentFile

// ---------------------------------------------------------
// UseCase для экспорта всех изображений из директории
// ---------------------------------------------------------
// Копирует все изображения из указанной локальной директории
// в выбранную пользователем папку через Storage Access Framework (SAF)
// Выполняется в фоновом потоке (IO dispatcher)
class ExportImagesUseCase(
    private val context: Context    // Контекст для доступа к ContentResolver и DocumentFile
) {
    // ---------------------------------------------------------
    // Оператор invoke - запуск экспорта изображений
    // ---------------------------------------------------------
    // sourceDir: локальная директория с изображениями (обычно filesDir/images)
    // targetUri: URI выбранной пользователем целевой папки (через SAF)
    // Возвращает Result<Unit> - успех или ошибка
    suspend operator fun invoke(sourceDir: File, targetUri: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {    // Переключение на IO поток для тяжёлых файловых операций
            try {
                // ---------------------------------------------------------
                // 1. Получение DocumentFile для целевой папки
                // ---------------------------------------------------------
                // Преобразование URI дерева в объект DocumentFile
                // Позволяет создавать файлы внутри выбранной папки
                val tree = DocumentFile.fromTreeUri(context, targetUri)
                    ?: return@withContext Result.failure(Exception("Invalid folder"))

                // ---------------------------------------------------------
                // 2. Обход всех файлов в исходной директории (рекурсивно)
                // ---------------------------------------------------------
                // walk() обходит все файлы и поддиректории (Depth-first)
                sourceDir.walk().forEach { file ->
                    // Обработка только файлов (пропуск директорий)
                    if (file.isFile) {
                        // ---------------------------------------------------------
                        // 3. Создание файла в целевой папке
                        // ---------------------------------------------------------
                        // Создание файла с MIME-типом image/jpeg и оригинальным именем
                        val newFile = tree.createFile("image/jpeg", file.name)
                            ?: return@forEach  // Пропуск, если не удалось создать файл

                        // ---------------------------------------------------------
                        // 4. Копирование содержимого
                        // ---------------------------------------------------------
                        // Открытие выходного потока для записи в целевой файл
                        context.contentResolver.openOutputStream(newFile.uri)?.use { out ->
                            // Копирование из входного потока (локальный файл) в выходной (SAF)
                            file.inputStream().use { input ->
                                input.copyTo(out)
                            }
                        }
                    }
                }

                // ---------------------------------------------------------
                // 5. Успешное завершение
                // ---------------------------------------------------------
                Result.success(Unit)

            } catch (e: Exception) {
                // ---------------------------------------------------------
                // Обработка ошибки
                // ---------------------------------------------------------
                Result.failure(e)
            }
        }
}