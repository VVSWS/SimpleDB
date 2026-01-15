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

class ExportDatabaseUseCase(
    private val context: Context,
    private val faultRepository: FaultRepository
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

            // ---------------------------------------------------------
            // 1. Load entries from repository (domain model)
            // ---------------------------------------------------------
            val entries = faultRepository.getEntriesWithImages()
            val export = ExportDatabase(entries.map { it.toExport() })

            // ---------------------------------------------------------
            // 2. Serialize JSON
            // ---------------------------------------------------------
            val jsonBytes = Json.encodeToString(export).toByteArray()

            val jsonUri = DocumentsContract.createDocument(
                resolver,
                dirUri,
                "application/json",
                "carfault_export.json"
            ) ?: throw IllegalStateException("Unable to create JSON file")

            resolver.openOutputStream(jsonUri)?.use { out ->
                out.write(jsonBytes)
            }

            // ---------------------------------------------------------
            // 3. Create images folder
            // ---------------------------------------------------------
            val imagesFolderUri = DocumentsContract.createDocument(
                resolver,
                dirUri,
                DocumentsContract.Document.MIME_TYPE_DIR,
                "images"
            ) ?: throw IllegalStateException("Unable to create images folder")

            // ---------------------------------------------------------
            // 4. Count total bytes for progress
            // ---------------------------------------------------------
            val totalBytes = entries
                .flatMap { it.imageUris }
                .sumOf { uri ->
                    when {
                        uri.startsWith("content://") ->
                            resolver.openInputStream(Uri.parse(uri))
                                ?.use { it.available().toLong() } ?: 0L

                        uri.startsWith("/") ->
                            File(uri).takeIf { it.exists() }?.length() ?: 0L

                        else ->
                            File(context.filesDir, uri).takeIf { it.exists() }?.length() ?: 0L
                    }
                }

            onProgress(DatabaseExportProgress.Started(totalBytes))

            var writtenBytes = 0L

            // ---------------------------------------------------------
            // 5. Copy images
            // ---------------------------------------------------------
            entries.forEach { entry ->

                val entryFolderUri = DocumentsContract.createDocument(
                    resolver,
                    imagesFolderUri,
                    DocumentsContract.Document.MIME_TYPE_DIR,
                    entry.entry.id.toString()
                ) ?: return@forEach

                entry.imageUris.forEach { uriString ->

                    val srcUri = Uri.parse(uriString)

                    val inputStream = when {
                        uriString.startsWith("content://") ->
                            resolver.openInputStream(srcUri)

                        uriString.startsWith("/") ->
                            File(uriString).takeIf { it.exists() }?.inputStream()

                        else ->
                            File(context.filesDir, uriString).takeIf { it.exists() }?.inputStream()
                    }

                    if (inputStream == null) return@forEach

                    val dstUri = DocumentsContract.createDocument(
                        resolver,
                        entryFolderUri,
                        "image/jpeg",
                        File(uriString).name
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
            // 6. Done
            // ---------------------------------------------------------
            onProgress(DatabaseExportProgress.Finished)
            Result.success(Unit)

        } catch (e: Exception) {
            onProgress(DatabaseExportProgress.Error(e.message ?: "Unknown error"))
            Result.failure(e)
        }
    }
}
