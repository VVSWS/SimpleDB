package ru.tusur.data.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.tusur.data.local.database.AppDatabase
import ru.tusur.data.mapper.toExport
import ru.tusur.domain.export.ExportDatabase
import ru.tusur.domain.usecase.database.DatabaseExportProgress
import java.io.File

class ExportDatabaseUseCase(
    private val context: Context,
    private val db: AppDatabase
) {

    suspend operator fun invoke(
        folderUri: Uri,
        onProgress: (DatabaseExportProgress) -> Unit
    ): Result<Unit> {
        return try {
            val resolver = context.contentResolver

            // Convert tree URI → directory document URI
            val docId = DocumentsContract.getTreeDocumentId(folderUri)
            val dirUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, docId)

            // Load entries
            val entries = db.entryDao().getAllEntriesWithImages()
            val export = ExportDatabase(entries.map { it.toExport() })

            // Serialize JSON
            val jsonBytes = Json.encodeToString(export).toByteArray()

            // Create JSON file
            val jsonUri = DocumentsContract.createDocument(
                resolver,
                dirUri,
                "application/json",
                "carfault_export.json"
            ) ?: throw IllegalStateException("Unable to create JSON file")

            resolver.openOutputStream(jsonUri)?.use { out ->
                out.write(jsonBytes)
            }

            // Create images folder
            val imagesFolderUri = DocumentsContract.createDocument(
                resolver,
                dirUri,
                DocumentsContract.Document.MIME_TYPE_DIR,
                "images"
            ) ?: throw IllegalStateException("Unable to create images folder")

            // Count total bytes for progress
            val totalBytes = entries
                .flatMap { it.images }
                .mapNotNull { image ->
                    val uri = image.uri

                    when {
                        uri.startsWith("content://") -> {
                            resolver.openInputStream(Uri.parse(uri))?.available()?.toLong()
                        }
                        uri.startsWith("/") -> {
                            File(uri).takeIf { it.exists() }?.length()
                        }
                        else -> {
                            File(context.filesDir, uri).takeIf { it.exists() }?.length()
                        }
                    }
                }
                .sum()

            onProgress(DatabaseExportProgress.Started(totalBytes))

            var writtenBytes = 0L

            // Copy images
            entries.forEach { entry ->
                val entryFolderUri = DocumentsContract.createDocument(
                    resolver,
                    imagesFolderUri,
                    DocumentsContract.Document.MIME_TYPE_DIR,
                    entry.entry.id.toString()
                ) ?: return@forEach

                entry.images.forEach { image ->
                    val srcUri = Uri.parse(image.uri)

                    val inputStream = when {
                        image.uri.startsWith("content://") ->
                            resolver.openInputStream(srcUri)

                        image.uri.startsWith("/") ->
                            File(image.uri).takeIf { it.exists() }?.inputStream()

                        else ->
                            File(context.filesDir, image.uri).takeIf { it.exists() }?.inputStream()
                    }

                    if (inputStream == null) return@forEach

                    val dstUri = DocumentsContract.createDocument(
                        resolver,
                        entryFolderUri,
                        "image/jpeg",
                        File(image.uri).name
                    ) ?: return@forEach

                    resolver.openOutputStream(dstUri)?.use { out ->
                        inputStream.use { input ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var read: Int

                            while (input.read(buffer).also { read = it } != -1) {
                                out.write(buffer, 0, read)
                                writtenBytes += read
                                onProgress(
                                    DatabaseExportProgress.Progress(
                                        writtenBytes,
                                        totalBytes
                                    )
                                )
                            }
                        }
                    }
                }
            }

            onProgress(DatabaseExportProgress.Finished)
            Result.success(Unit)

        } catch (e: Exception) {
            onProgress(DatabaseExportProgress.Error(e.message ?: "Unknown error"))
            Result.failure(e)
        }
    }
}
