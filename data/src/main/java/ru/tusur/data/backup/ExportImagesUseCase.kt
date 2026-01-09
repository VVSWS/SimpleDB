package ru.tusur.data.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.documentfile.provider.DocumentFile


class ExportImagesUseCase(
    private val context: Context
) {
    suspend operator fun invoke(sourceDir: File, targetUri: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val tree = DocumentFile.fromTreeUri(context, targetUri)
                    ?: return@withContext Result.failure(Exception("Invalid folder"))

                sourceDir.walk().forEach { file ->
                    if (file.isFile) {
                        val newFile = tree.createFile("image/jpeg", file.name)
                        context.contentResolver.openOutputStream(newFile!!.uri)?.use { out ->
                            file.inputStream().use { it.copyTo(out) }
                        }
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
