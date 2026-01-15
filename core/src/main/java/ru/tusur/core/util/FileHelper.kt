package ru.tusur.core.util

import android.content.Context
import java.io.File
import java.io.IOException

object FileHelper {

    private const val DB_PREFIX = "BD_"
    private const val ACTIVE_DB_NAME = "${DB_PREFIX}active.db"

    /**
     * Active DB always lives in internal app storage:
     * /data/data/<package>/databases/BD_active.db
     */
    fun getActiveDatabaseFile(context: Context): File {
        val file = context.getDatabasePath(ACTIVE_DB_NAME)
        file.parentFile?.mkdirs()
        return file
    }

    /**
     * Copy a file, overwriting the destination.
     */
    @Throws(IOException::class)
    fun copyFile(source: File, dest: File) {
        dest.parentFile?.mkdirs()
        source.inputStream().use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
