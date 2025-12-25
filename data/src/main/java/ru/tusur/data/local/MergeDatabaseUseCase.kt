package ru.tusur.data.local

import android.content.Context
import ru.tusur.core.util.FileHelper
import ru.tusur.domain.repository.FaultRepository
import java.io.File

class MergeDatabaseUseCase(
    private val context: Context,
    private val databaseProvider: DatabaseProvider,
    private val faultRepository: FaultRepository
) {

    suspend operator fun invoke(externalDb: File): Int {
        // Copy external DB to temp file
        val tempFile = File(context.cacheDir, "temp_merge.db")
        FileHelper.copyFile(externalDb, tempFile)

        // Open external DB
        val external = databaseProvider.getDatabase(tempFile)

        // Open active DB
        val active = databaseProvider.getDatabase(
            FileHelper.getActiveDatabaseFile(context)
        )

        val externalEntries = external.entryDao().getAllSync()
        val activeEntries = active.entryDao().getAllSync()

        val newEntries = externalEntries.filter { e ->
            activeEntries.none { it.id == e.id }
        }

        newEntries.forEach { active.entryDao().insert(it) }

        return newEntries.size
    }
}
