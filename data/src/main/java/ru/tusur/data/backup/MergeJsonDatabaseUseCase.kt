package ru.tusur.data.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.serialization.json.Json
import ru.tusur.domain.export.ExportDatabase
import ru.tusur.domain.model.*
import ru.tusur.domain.repository.FaultRepository
import ru.tusur.domain.repository.ReferenceDataRepository
import java.io.File

// ---------------------------------------------------------
// UseCase для слияния экспортированной базы данных с текущей
// ---------------------------------------------------------
// Импортирует все записи из JSON-файла и изображений из выбранной папки
// Добавляет новые записи, не удаляя существующие (слияние, а не замена)
// Отслеживает прогресс через callback
class MergeJsonDatabaseUseCase(
    private val context: Context,                         // Контекст для доступа к filesDir
    private val faultRepository: FaultRepository,         // Репозиторий для записей
    private val referenceRepository: ReferenceDataRepository  // Репозиторий для справочников
) {

    // ---------------------------------------------------------
    // Оператор invoke - запуск слияния
    // ---------------------------------------------------------
    // folderUri: URI папки с экспортированными данными (JSON + images/)
    // onProgress: Callback для прогресса (текущий шаг, всего шагов)
    // Возвращает Result<Int> - количество слитых записей или ошибку
    suspend operator fun invoke(
        folderUri: Uri,
        onProgress: (step: Int, totalSteps: Int) -> Unit = { _, _ -> }
    ): Result<Int> {
        return try {
            val resolver = context.contentResolver

            // ---------------------------------------------------------
            // Преобразование URI дерева в URI директории
            // ---------------------------------------------------------
            val rootDocId = DocumentsContract.getTreeDocumentId(folderUri)
            val rootDirUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, rootDocId)

            // ---------------------------------------------------------
            // 1. Поиск JSON-файла в корневой директории
            // ---------------------------------------------------------
            val jsonUri = findJsonFile(resolver, rootDirUri)
                ?: return Result.failure(Exception("JSON backup file not found"))

            // ---------------------------------------------------------
            // 2. Чтение и десериализация JSON
            // ---------------------------------------------------------
            val jsonString = resolver.openInputStream(jsonUri)
                ?.use { it.readBytes().decodeToString() }
                ?: return Result.failure(Exception("Unable to read JSON file"))

            val export = Json.decodeFromString<ExportDatabase>(jsonString)

            // ---------------------------------------------------------
            // 3. Поиск папки с изображениями
            // ---------------------------------------------------------
            val imagesFolderUri = findImagesFolder(resolver, rootDirUri)
                ?: return Result.failure(Exception("Images folder not found"))

            // ---------------------------------------------------------
            // 4. Подготовка к слиянию записей
            // ---------------------------------------------------------
            val totalSteps = export.entries.size  // Общее количество записей для импорта
            var mergedCount = 0                    // Счётчик успешно импортированных записей

            // ---------------------------------------------------------
            // 5. Обработка каждой записи из экспорта
            // ---------------------------------------------------------
            export.entries.forEachIndexed { index, dto ->

                // Отправка прогресса (текущая запись / всего)
                onProgress(index, totalSteps)

                // ---------------------------------------------------------
                // Валидация обязательных полей DTO
                // ---------------------------------------------------------
                val brandName = dto.brand ?: error("Backup entry missing brand")
                val locationName = dto.location ?: error("Backup entry missing location")
                val modelName = dto.modelName ?: error("Backup entry missing modelName")
                val yearValue = dto.year ?: error("Backup entry missing year")

                // ---------------------------------------------------------
                // Преобразование в доменные объекты
                // ---------------------------------------------------------
                val brand = Brand(brandName)
                val location = Location(locationName)
                val year = Year(yearValue)
                val model = Model(
                    name = modelName,
                    brand = brand,
                    year = year
                )

                // ---------------------------------------------------------
                // Вставка справочных данных (игнорирование дубликатов)
                // ---------------------------------------------------------
                referenceRepository.addBrand(brand)
                referenceRepository.addLocation(location)
                referenceRepository.addYear(year)
                referenceRepository.addModel(model)

                // ---------------------------------------------------------
                // Создание записи о неисправности
                // ---------------------------------------------------------
                val newEntry = FaultEntry(
                    id = 0,                              // 0 = автогенерация ID
                    timestamp = dto.timestamp,
                    year = year,
                    brand = brand,
                    model = model,
                    location = location,
                    title = dto.title,
                    description = dto.description,
                    imageUris = emptyList()              // Изображения добавим позже
                )

                // Вставка записи в БД
                val newEntryId = faultRepository.createEntry(newEntry)

                // ---------------------------------------------------------
                // 6. Копирование изображений для записи
                // ---------------------------------------------------------
                val newImageUris = copyImagesForEntry(
                    context = context,
                    resolver = resolver,
                    imagesFolderUri = imagesFolderUri,
                    oldEntryId = dto.id,          // ID из экспортированной БД
                    newEntryId = newEntryId,      // Новый ID в текущей БД
                    imageNames = dto.images
                )

                // Привязка скопированных изображений к записи
                newImageUris.forEach { uri ->
                    faultRepository.addImageToEntry(newEntryId, uri)
                }

                mergedCount++  // Увеличение счётчика успешно импортированных записей
            }

            // ---------------------------------------------------------
            // Завершение слияния
            // ---------------------------------------------------------
            onProgress(totalSteps, totalSteps)
            Result.success(mergedCount)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------
    // Вспомогательные методы
    // ------------------------------------------------------------

    // ---------------------------------------------------------
    // Поиск JSON-файла в корневой директории
    // ---------------------------------------------------------
    private fun findJsonFile(resolver: android.content.ContentResolver, dirUri: Uri): Uri? {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            dirUri,
            DocumentsContract.getDocumentId(dirUri)
        )

        resolver.query(childrenUri, arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        ), null, null, null)?.use { cursor ->

            val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val mimeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

            while (cursor.moveToNext()) {
                val docId = cursor.getString(idIndex)
                val mime = cursor.getString(mimeIndex)

                // Проверка MIME-типа JSON
                if (mime == "application/json" || mime == "text/json") {
                    return DocumentsContract.buildDocumentUriUsingTree(dirUri, docId)
                }
            }
        }

        return null
    }

    // ---------------------------------------------------------
    // Поиск папки "images" в корневой директории
    // ---------------------------------------------------------
    private fun findImagesFolder(resolver: android.content.ContentResolver, dirUri: Uri): Uri? {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            dirUri,
            DocumentsContract.getDocumentId(dirUri)
        )

        resolver.query(childrenUri, arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        ), null, null, null)?.use { cursor ->

            val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

            while (cursor.moveToNext()) {
                val docId = cursor.getString(idIndex)
                val name = cursor.getString(nameIndex)
                val mime = cursor.getString(mimeIndex)

                // Поиск директории с именем "images"
                if (mime == DocumentsContract.Document.MIME_TYPE_DIR && name == "images") {
                    return DocumentsContract.buildDocumentUriUsingTree(dirUri, docId)
                }
            }
        }

        return null
    }

    // ---------------------------------------------------------
    // Копирование изображений для конкретной записи
    // ---------------------------------------------------------
    private fun copyImagesForEntry(
        context: Context,
        resolver: android.content.ContentResolver,
        imagesFolderUri: Uri,
        oldEntryId: Long,
        newEntryId: Long,
        imageNames: List<String>
    ): List<String> {

        val resultUris = mutableListOf<String>()

        // Поиск папки со старым ID записи внутри images/
        val entryFolderUri = findEntryImageFolder(resolver, imagesFolderUri, oldEntryId)
            ?: return emptyList()

        // Целевая директория в хранилище приложения
        val dstDir = File(context.filesDir, "images/$newEntryId")
        dstDir.mkdirs()

        // Копирование каждого изображения
        imageNames.forEach { rawName ->
            val cleanName = File(rawName).name  // Очистка имени файла (удаление пути)

            // Поиск файла изображения в папке записи
            val srcUri = findImageFile(resolver, entryFolderUri, cleanName)
                ?: return@forEach

            // Целевой файл
            val dstFile = File(dstDir, cleanName)

            // Копирование содержимого из SAF в локальный файл
            resolver.openInputStream(srcUri)?.use { input ->
                dstFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // ---------------------------------------------------------
            // Создание content:// URI для доступа из других компонентов
            // ---------------------------------------------------------
            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",  // Authority из AndroidManifest
                dstFile
            )

            resultUris.add(contentUri.toString())
        }

        return resultUris
    }

    // ---------------------------------------------------------
    // Поиск папки записи внутри images/ (по старому ID)
    // ---------------------------------------------------------
    private fun findEntryImageFolder(
        resolver: android.content.ContentResolver,
        imagesFolderUri: Uri,
        oldEntryId: Long
    ): Uri? {

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            imagesFolderUri,
            DocumentsContract.getDocumentId(imagesFolderUri)
        )

        resolver.query(childrenUri, arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        ), null, null, null)?.use { cursor ->

            val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

            while (cursor.moveToNext()) {
                val docId = cursor.getString(idIndex)
                val name = cursor.getString(nameIndex)
                val mime = cursor.getString(mimeIndex)

                // Поиск директории, имя которой совпадает со старым ID записи
                if (mime == DocumentsContract.Document.MIME_TYPE_DIR && name == oldEntryId.toString()) {
                    return DocumentsContract.buildDocumentUriUsingTree(imagesFolderUri, docId)
                }
            }
        }

        return null
    }

    // ---------------------------------------------------------
    // Поиск файла изображения по имени внутри папки записи
    // ---------------------------------------------------------
    private fun findImageFile(
        resolver: android.content.ContentResolver,
        entryFolderUri: Uri,
        imageName: String
    ): Uri? {

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            entryFolderUri,
            DocumentsContract.getDocumentId(entryFolderUri)
        )

        resolver.query(childrenUri, arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME
        ), null, null, null)?.use { cursor ->

            val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val docId = cursor.getString(idIndex)
                val name = cursor.getString(nameIndex)

                // Сравнение имени файла
                if (name == imageName) {
                    return DocumentsContract.buildDocumentUriUsingTree(entryFolderUri, docId)
                }
            }
        }

        return null
    }
}