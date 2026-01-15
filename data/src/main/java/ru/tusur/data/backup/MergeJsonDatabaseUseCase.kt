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

class MergeJsonDatabaseUseCase(
    private val context: Context,
    private val faultRepository: FaultRepository,
    private val referenceRepository: ReferenceDataRepository
) {

    suspend operator fun invoke(
        folderUri: Uri,
        onProgress: (step: Int, totalSteps: Int) -> Unit = { _, _ -> }
    ): Result<Int> {
        return try {
            val resolver = context.contentResolver

            // Convert tree URI → directory document URI
            val rootDocId = DocumentsContract.getTreeDocumentId(folderUri)
            val rootDirUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, rootDocId)

            // ---------------------------------------------------------
            // 1. Locate JSON file
            // ---------------------------------------------------------
            val jsonUri = findJsonFile(resolver, rootDirUri)
                ?: return Result.failure(Exception("JSON backup file not found"))

            // ---------------------------------------------------------
            // 2. Read JSON
            // ---------------------------------------------------------
            val jsonString = resolver.openInputStream(jsonUri)
                ?.use { it.readBytes().decodeToString() }
                ?: return Result.failure(Exception("Unable to read JSON file"))

            val export = Json.decodeFromString<ExportDatabase>(jsonString)

            // ---------------------------------------------------------
            // 3. Locate images folder
            // ---------------------------------------------------------
            val imagesFolderUri = findImagesFolder(resolver, rootDirUri)
                ?: return Result.failure(Exception("Images folder not found"))

            val totalSteps = export.entries.size
            var mergedCount = 0

            // ---------------------------------------------------------
            // 4. Merge entries
            // ---------------------------------------------------------
            export.entries.forEachIndexed { index, dto ->

                onProgress(index, totalSteps)

                // Validate required fields
                val brandName = dto.brand ?: error("Backup entry missing brand")
                val locationName = dto.location ?: error("Backup entry missing location")
                val modelName = dto.modelName ?: error("Backup entry missing modelName")
                val yearValue = dto.year ?: error("Backup entry missing year")

                // Convert to domain models
                val brand = Brand(brandName)
                val location = Location(locationName)
                val year = Year(yearValue)
                val model = Model(
                    name = modelName,
                    brand = brand,
                    year = year
                )

                // Insert dictionary values
                referenceRepository.addBrand(brand)
                referenceRepository.addLocation(location)
                referenceRepository.addYear(year)
                referenceRepository.addModel(model)

                // ---------------------------------------------------------
                // Create FaultEntry (domain model)
                // ---------------------------------------------------------
                val newEntry = FaultEntry(
                    id = 0, // auto-generate
                    timestamp = dto.timestamp,
                    year = year,
                    brand = brand,
                    model = model,
                    location = location,
                    title = dto.title,
                    description = dto.description,
                    imageUris = emptyList()
                )

                // Insert entry
                val newEntryId = faultRepository.createEntry(newEntry)

                // ---------------------------------------------------------
                // 5. Copy images
                // ---------------------------------------------------------
                val newImageUris = copyImagesForEntry(
                    context = context,
                    resolver = resolver,
                    imagesFolderUri = imagesFolderUri,
                    oldEntryId = dto.id,
                    newEntryId = newEntryId,
                    imageNames = dto.images
                )

                // Attach images to entry
                newImageUris.forEach { uri ->
                    faultRepository.addImageToEntry(newEntryId, uri)
                }

                mergedCount++
            }

            onProgress(totalSteps, totalSteps)
            Result.success(mergedCount)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------
    // Helpers (unchanged)
    // ------------------------------------------------------------

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

                if (mime == "application/json" || mime == "text/json") {
                    return DocumentsContract.buildDocumentUriUsingTree(dirUri, docId)
                }
            }
        }

        return null
    }

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

                if (mime == DocumentsContract.Document.MIME_TYPE_DIR && name == "images") {
                    return DocumentsContract.buildDocumentUriUsingTree(dirUri, docId)
                }
            }
        }

        return null
    }

    private fun copyImagesForEntry(
        context: Context,
        resolver: android.content.ContentResolver,
        imagesFolderUri: Uri,
        oldEntryId: Long,
        newEntryId: Long,
        imageNames: List<String>
    ): List<String> {

        val resultUris = mutableListOf<String>()

        val entryFolderUri = findEntryImageFolder(resolver, imagesFolderUri, oldEntryId)
            ?: return emptyList()

        val dstDir = File(context.filesDir, "images/$newEntryId")
        dstDir.mkdirs()

        imageNames.forEach { rawName ->
            val cleanName = File(rawName).name

            val srcUri = findImageFile(resolver, entryFolderUri, cleanName)
                ?: return@forEach

            val dstFile = File(dstDir, cleanName)

            resolver.openInputStream(srcUri)?.use { input ->
                dstFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                dstFile
            )

            resultUris.add(contentUri.toString())
        }

        return resultUris
    }

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

                if (mime == DocumentsContract.Document.MIME_TYPE_DIR && name == oldEntryId.toString()) {
                    return DocumentsContract.buildDocumentUriUsingTree(imagesFolderUri, docId)
                }
            }
        }

        return null
    }

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

                if (name == imageName) {
                    return DocumentsContract.buildDocumentUriUsingTree(entryFolderUri, docId)
                }
            }
        }

        return null
    }
}
