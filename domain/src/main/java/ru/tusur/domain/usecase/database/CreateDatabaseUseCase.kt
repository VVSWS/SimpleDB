package ru.tusur.domain.usecase.database

import android.content.Context
import ru.tusur.core.util.FileHelper
import java.io.File

/**
 * Creates (or recreates) the active database file in internal storage.
 * Does NOT populate schema; Room will do that on first open.
 */
class CreateDatabaseUseCase(
    private val context: Context
) {

    operator fun invoke(): File {
        val dbFile = FileHelper.getActiveDatabaseFile(context)

        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            dbFile.createNewFile()
        }

        return dbFile
    }
}
