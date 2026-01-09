package ru.tusur.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ImportImagesUseCase(
    private val context: Context
) {
    suspend operator fun invoke(targetDir: File, sourceUri: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val tree = DocumentFile.fromTreeUri(context, sourceUri)
                    ?: return@withContext Result.failure(Exception("Invalid folder"))

                tree.listFiles().forEach { doc ->
                    val outFile = File(targetDir, doc.name ?: "img.jpg")
                    context.contentResolver.openInputStream(doc.uri)?.use { input ->
                        outFile.outputStream().use { input.copyTo(it) }
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
