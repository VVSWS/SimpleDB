package ru.tusur.data.usecase

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.tusur.data.local.DatabaseProvider
import ru.tusur.domain.repository.FaultRepository
import java.io.File

data class CurrentDatabaseInfo(
    val filename: String?,
    val entryCount: Int,
    val dbSizeBytes: Long,
    val imageCount: Int,
    val imagesFolderSizeBytes: Long
)

class GetCurrentDatabaseInfoUseCase(
    private val context: Context,
    private val provider: DatabaseProvider,
    private val faultRepository: FaultRepository
) {

    /**
     * Returns a fresh snapshot of all database metrics.
     * Called manually by MainViewModel whenever DB changes.
     */
    suspend operator fun invoke(): CurrentDatabaseInfo = withContext(Dispatchers.IO) {

        // Entry count
        val entryCount = faultRepository.getEntryCount()

        // Fake DB size (approximate)
        val dbSize = entryCount * 350L

        // Fake filename (still useful for UI)
        val dbName = "SimpleDB"

        // Image count
        val imageCount = faultRepository
            .getEntriesWithImages()
            .sumOf { it.imageUris.size }

        // Images folder size
        val imagesDir = File(context.filesDir, "images")
        val imagesFolderSize = if (imagesDir.exists()) {
            imagesDir.walk().filter { it.isFile }.sumOf { it.length() }
        } else 0L

        CurrentDatabaseInfo(
            filename = dbName,
            entryCount = entryCount,
            dbSizeBytes = dbSize,
            imageCount = imageCount,
            imagesFolderSizeBytes = imagesFolderSize
        )
    }
}
