package ru.tusur.data.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.serialization.json.Json
import ru.tusur.data.mapper.toExport
import ru.tusur.domain.export.ExportDatabase
import ru.tusur.domain.repository.FaultRepository
import ru.tusur.domain.usecase.database.DatabaseExportProgress
import java.io.File

// ---------------------------------------------------------
// UseCase для экспорта базы данных и изображений в выбранную папку
// ---------------------------------------------------------
// Экспортирует все записи о неисправностях в JSON-файл
// Копирует все связанные изображения в структуру папок
// Предоставляет прогресс операции (начало, ход, завершение, ошибка)
class ExportDatabaseUseCase(
    private val context: Context,                    // Контекст приложения для доступа к ContentResolver
    private val faultRepository: FaultRepository     // Репозиторий для получения записей
) {

    // ---------------------------------------------------------
    // Оператор invoke - запуск экспорта
    // ---------------------------------------------------------
    // folderUri: URI выбранной пользователем папки (через SAF - Storage Access Framework)
    // onProgress: Callback для отслеживания прогресса экспорта
    // Возвращает Result<Unit> - успех или ошибка
    suspend operator fun invoke(
        folderUri: Uri,
        onProgress: (DatabaseExportProgress) -> Unit
    ): Result<Unit> {
        return try {
            val resolver = context.contentResolver

            // ---------------------------------------------------------
            // Преобразование URI дерева в URI директории
            // ---------------------------------------------------------
            // Получение ID документа из tree URI
            val docId = DocumentsContract.getTreeDocumentId(folderUri)
            // Построение URI для директории внутри выбранного дерева
            val dirUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, docId)

            // ---------------------------------------------------------
            // 1. Загрузка всех записей из репозитория
            // ---------------------------------------------------------
            // Получение списка записей с их изображениями (доменная модель)
            val entries = faultRepository.getEntriesWithImages()
            // Преобразование в экспортируемый формат и обёртывание в контейнер
            val export = ExportDatabase(entries.map { it.toExport() })

            // ---------------------------------------------------------
            // 2. Сериализация данных в JSON
            // ---------------------------------------------------------
            // Преобразование объекта ExportDatabase в строку JSON и затем в массив байтов
            val jsonBytes = Json.encodeToString(export).toByteArray()

            // Создание JSON-файла в выбранной директории
            val jsonUri = DocumentsContract.createDocument(
                resolver,
                dirUri,
                "application/json",          // MIME-тип JSON
                "carfault_export.json"       // Имя файла экспорта
            ) ?: throw IllegalStateException("Unable to create JSON file")

            // Запись JSON-байтов в созданный файл
            resolver.openOutputStream(jsonUri)?.use { out ->
                out.write(jsonBytes)
            }

            // ---------------------------------------------------------
            // 3. Создание папки для изображений
            // ---------------------------------------------------------
            val imagesFolderUri = DocumentsContract.createDocument(
                resolver,
                dirUri,
                DocumentsContract.Document.MIME_TYPE_DIR,  // Тип "директория"
                "images"                                    // Имя папки
            ) ?: throw IllegalStateException("Unable to create images folder")

            // ---------------------------------------------------------
            // 4. Подсчёт общего размера всех изображений для прогресса
            // ---------------------------------------------------------
            val totalBytes = entries
                .flatMap { it.imageUris }                    // Сбор всех URI изображений
                .sumOf { uri ->                              // Суммирование размеров
                    when {
                        // Обработка content:// URI (из галереи)
                        uri.startsWith("content://") ->
                            resolver.openInputStream(Uri.parse(uri))
                                ?.use { it.available().toLong() } ?: 0L

                        // Обработка абсолютного пути
                        uri.startsWith("/") ->
                            File(uri).takeIf { it.exists() }?.length() ?: 0L

                        // Обработка относительного пути (внутри filesDir)
                        else ->
                            File(context.filesDir, uri).takeIf { it.exists() }?.length() ?: 0L
                    }
                }

            // Отправка события "начало экспорта" с общим размером
            onProgress(DatabaseExportProgress.Started(totalBytes))

            var writtenBytes = 0L  // Счётчик скопированных байтов

            // ---------------------------------------------------------
            // 5. Копирование изображений в структуру папок
            // ---------------------------------------------------------
            // Структура: images/{entry_id}/{image_filename}.jpg
            entries.forEach { entry ->

                // Создание папки для текущей записи (название = ID записи)
                val entryFolderUri = DocumentsContract.createDocument(
                    resolver,
                    imagesFolderUri,
                    DocumentsContract.Document.MIME_TYPE_DIR,
                    entry.entry.id.toString()
                ) ?: return@forEach  // Пропуск, если не удалось создать папку

                // Копирование каждого изображения записи
                entry.imageUris.forEach { uriString ->

                    val srcUri = Uri.parse(uriString)  // Преобразование строки в URI

                    // Определение источника изображения в зависимости от формата пути
                    val inputStream = when {
                        uriString.startsWith("content://") ->
                            resolver.openInputStream(srcUri)

                        uriString.startsWith("/") ->
                            File(uriString).takeIf { it.exists() }?.inputStream()

                        else ->
                            File(context.filesDir, uriString).takeIf { it.exists() }?.inputStream()
                    }

                    // Пропуск, если поток не удалось открыть
                    if (inputStream == null) return@forEach

                    // Создание файла для изображения внутри папки записи
                    val dstUri = DocumentsContract.createDocument(
                        resolver,
                        entryFolderUri,
                        "image/jpeg",                           // MIME-тип JPEG
                        File(uriString).name                    // Оригинальное имя файла
                    ) ?: return@forEach

                    // Копирование содержимого с буферизацией и отчётом о прогрессе
                    resolver.openOutputStream(dstUri)?.use { out ->
                        inputStream.use { input ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)  // Буфер 8 КБ
                            var read: Int

                            // Чтение блоками и запись
                            while (input.read(buffer).also { read = it } != -1) {
                                out.write(buffer, 0, read)
                                writtenBytes += read  // Обновление счётчика скопированных байтов

                                // Отправка события прогресса (скопировано / всего)
                                onProgress(
                                    DatabaseExportProgress.Progress(
                                        writtenBytes = writtenBytes,
                                        totalBytes = totalBytes
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ---------------------------------------------------------
            // 6. Завершение экспорта
            // ---------------------------------------------------------
            onProgress(DatabaseExportProgress.Finished)
            Result.success(Unit)

        } catch (e: Exception) {
            // ---------------------------------------------------------
            // Обработка ошибки
            // ---------------------------------------------------------
            onProgress(DatabaseExportProgress.Error(e.message ?: "Unknown error"))
            Result.failure(e)
        }
    }

    // ---------------------------------------------------------
    // Константа: размер буфера для копирования (8 КБ)
    // ---------------------------------------------------------
    companion object {
        private const val DEFAULT_BUFFER_SIZE = 8192
    }
}