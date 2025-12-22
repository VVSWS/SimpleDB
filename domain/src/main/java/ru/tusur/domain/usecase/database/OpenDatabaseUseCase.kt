package ru.tusur.domain.usecase.database

import android.content.Context
import ru.tusur.core.util.FileHelper
import java.io.File

/**
 * Takes a source DB file (e.g., copied from SAF Uri to cache),
 * normalizes its name, and copies it into the active DB location.
 */
class OpenDatabaseUseCase(
    private val context: Context
) {

    operator fun invoke(tempSourceFile: File): File {
        val activeDbFile = FileHelper.getActiveDatabaseFile(context)
        FileHelper.copyFile(tempSourceFile, activeDbFile)
        return activeDbFile
    }
}
