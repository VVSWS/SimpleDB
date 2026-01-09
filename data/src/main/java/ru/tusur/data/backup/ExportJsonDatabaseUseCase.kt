package ru.tusur.data.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.tusur.data.local.database.AppDatabase
import ru.tusur.domain.export.ExportDatabase
import ru.tusur.data.mapper.toExport
import kotlinx.serialization.json.Json



class ExportJsonDatabaseUseCase(
    private val context: Context,
    private val db: AppDatabase
) {
    suspend operator fun invoke(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entries = db.entryDao().getAllEntriesWithImages()
            val export = ExportDatabase(entries = entries.map { it.toExport() })
            val json = Json.encodeToString(export)

            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(json.toByteArray())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
