package ru.tusur.data.local

import android.content.Context
import ru.tusur.core.util.FileHelper
import java.io.File

class MergeDatabaseManager(
    private val context: Context,
    private val databaseProvider: DatabaseProvider
) {

    suspend fun merge(externalDb: File): Int {
        val tempFile = File(context.cacheDir, "temp_merge.db")
        FileHelper.copyFile(externalDb, tempFile)

        val external = databaseProvider.getDatabase(tempFile)
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
