package ru.tusur.domain.usecase.database

import android.content.Context
import ru.tusur.core.util.FileHelper
import java.io.File

class OpenDatabaseUseCase(
    private val context: Context
) {

    operator fun invoke(file: File): File {
        if (!file.exists()) {
            throw IllegalArgumentException("Database file does not exist")
        }

        // Basic corruption check
        if (file.length() < 100) {
            throw IllegalStateException("Database file is corrupted or empty")
        }

        val activeFile = FileHelper.getActiveDatabaseFile(context)
        FileHelper.copyFile(file, activeFile)

        return activeFile
    }
}
