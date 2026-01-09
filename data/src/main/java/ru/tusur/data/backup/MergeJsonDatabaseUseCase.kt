package ru.tusur.data.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.room.withTransaction
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import ru.tusur.data.local.database.AppDatabase
import ru.tusur.data.local.entity.*
import ru.tusur.domain.export.ExportDatabase
import java.io.File

class MergeJsonDatabaseUseCase(
    private val context: Context,
    private val db: AppDatabase
) {

    suspend operator fun invoke(folderUri: Uri): Result<Int> {
        return try {
            val resolver = context.contentResolver

            // Convert tree URI → directory document URI
            val rootDocId = DocumentsContract.getTreeDocumentId(folderUri)
            val rootDirUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, rootDocId)

            // 1. Locate JSON file
            val jsonUri = findJsonFile(resolver, rootDirUri)
                ?: return Result.failure(Exception("JSON backup file not found"))

            // 2. Read JSON
            val jsonString = resolver.openInputStream(jsonUri)?.use { it.readBytes().decodeToString() }
                ?: return Result.failure(Exception("Unable to read JSON file"))

            val export = Json.decodeFromString<ExportDatabase>(jsonString)

            // 3. Locate images folder
            val imagesFolderUri = findImagesFolder(resolver, rootDirUri)
                ?: return Result.failure(Exception("Images folder not found"))

            var mergedCount = 0

            db.withTransaction {

                export.entries.forEach { exportedEntry ->

                    // -------------------------------
                    // Null-safety validation
                    // -------------------------------
                    val brand = exportedEntry.brand
                        ?: error("Backup entry is missing brand")

                    val location = exportedEntry.location
                        ?: error("Backup entry is missing location")

                    val modelName = exportedEntry.modelName
                        ?: error("Backup entry is missing modelName")

                    val year = exportedEntry.year
                        ?: error("Backup entry is missing year")

                    // -------------------------------
                    // Insert dictionary values
                    // -------------------------------
                    db.brandDao().insertIfMissing(BrandEntity(brand))
                    db.locationDao().insertIfMissing(LocationEntity(location))
                    db.yearDao().insertIfMissing(YearEntity(year))

                    // Composite ModelEntity
                    db.modelDao().insertIfMissing(
                        ModelEntity(
                            name = modelName,
                            brandName = brand,
                            yearValue = year
                        )
                    )

                    // -------------------------------
                    // Insert entry
                    // -------------------------------
                    val newEntryId = db.entryDao().insert(
                        EntryEntity(
                            id = 0,
                            timestamp = exportedEntry.timestamp,
                            year = year,
                            brand = brand,
                            modelName = modelName,
                            modelBrand = exportedEntry.modelBrand,
                            modelYear = exportedEntry.modelYear,
                            location = location,
                            title = exportedEntry.title,
                            description = exportedEntry.description,
                            notes = exportedEntry.notes
                        )
                    )

                    // -------------------------------
                    // Copy images
                    // -------------------------------
                    val newImagePaths = copyImagesForEntry(
                        context = context,
                        resolver = resolver,
                        imagesFolderUri = imagesFolderUri,
                        oldEntryId = exportedEntry.id,
                        newEntryId = newEntryId,
                        imageNames = exportedEntry.images
                    )

                    // Insert images
                    newImagePaths.forEach { path ->
                        db.entryImageDao().insertImage(
                            EntryImageEntity(
                                uri = path,
                                entryId = newEntryId
                            )
                        )
                    }

                    mergedCount++
                }
            }

            Result.success(mergedCount)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------
    // Helpers
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

        val resultPaths = mutableListOf<String>()

        // Locate entry folder inside images/
        val entryFolderUri = findEntryImageFolder(resolver, imagesFolderUri, oldEntryId)
            ?: return emptyList()

        // Prepare destination folder
        val dstDir = File(context.filesDir, "images/$newEntryId")
        dstDir.mkdirs()

        // Copy each image
        imageNames.forEach { rawName ->
            val cleanName = File(rawName).name   // normalize filename

            val srcUri = findImageFile(resolver, entryFolderUri, cleanName)
                ?: return@forEach

            val dstFile = File(dstDir, cleanName)

            resolver.openInputStream(srcUri)?.use { input ->
                dstFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            resultPaths.add(dstFile.absolutePath)
        }

        return resultPaths
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
