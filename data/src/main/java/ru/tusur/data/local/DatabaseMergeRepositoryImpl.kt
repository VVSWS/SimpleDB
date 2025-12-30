package ru.tusur.data.local

import android.content.Context
import ru.tusur.core.util.FileHelper
import ru.tusur.domain.repository.DatabaseMergeRepository
import java.io.File

class DatabaseMergeRepositoryImpl(
    private val context: Context,
    private val mergeManager: MergeDatabaseManager
) : DatabaseMergeRepository {

    override suspend fun mergeDatabase(externalDb: File): Int {
        val tempFile = File(context.cacheDir, "temp_merge.db")
        FileHelper.copyFile(externalDb, tempFile)

        return try {
            mergeManager.merge(tempFile)
        } finally {
            tempFile.delete()
        }
    }
}
