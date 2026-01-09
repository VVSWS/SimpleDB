package ru.tusur.data.backup

import android.content.Context
import kotlinx.serialization.json.Json
import ru.tusur.data.local.database.AppDatabase
import ru.tusur.data.mapper.toEntity
import ru.tusur.data.mapper.toImageEntities
import ru.tusur.domain.export.ExportDatabase
import java.io.File
import java.io.InputStream

class ImportJsonDatabaseUseCase(
    private val context: Context,
    private val db: AppDatabase
) {

    suspend operator fun invoke(inputStream: InputStream): Result<Unit> {
        return try {
            val json = inputStream.bufferedReader().use { it.readText() }
            val export = Json.decodeFromString<ExportDatabase>(json)

            val backupDir = File(context.filesDir, "backup/images")

            export.entries.forEach { dto ->
                // 1. Insert entry
                db.entryDao().insertEntry(dto.toEntity())

                // 2. Insert image rows
                db.entryImageDao().insertImages(dto.toImageEntities())

                // 3. Copy images from backup folder into app storage
                val entryDir = File(backupDir, dto.id.toString())
                if (entryDir.exists()) {
                    entryDir.listFiles()?.forEach { src ->
                        val dst = File(dto.images.firstOrNull()?.let { File(it).parent } ?: "", src.name)
                        src.copyTo(dst, overwrite = true)
                    }
                }
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
