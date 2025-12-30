package ru.tusur.domain.usecase.database

import android.content.Context
import android.net.Uri
import ru.tusur.core.util.FileHelper

class ExportDatabaseUseCase(
    private val context: Context
) {

    /**
     * Exports the active database into the SAF Uri selected by the user.
     */
    suspend operator fun invoke(targetUri: Uri): Result<Unit> {
        return try {
            val activeDb = FileHelper.getActiveDatabaseFile(context)

            context.contentResolver.openOutputStream(targetUri)?.use { output ->
                activeDb.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: return Result.failure(IllegalStateException("Cannot open output stream"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
