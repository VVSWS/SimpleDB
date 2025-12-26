package ru.tusur.domain.usecase.database

import android.content.Context
import ru.tusur.core.util.FileHelper
import java.io.File

class CreateDatabaseUseCase(
    private val context: Context
) {
    operator fun invoke(): File {
        val dbFile = FileHelper.getActiveDatabaseFile(context)

        dbFile.parentFile?.mkdirs()

        if (dbFile.exists() && dbFile.length() < 100) {
            dbFile.delete()
        }

        if (!dbFile.exists()) {
            dbFile.createNewFile()
        }

        return dbFile
    }
}


