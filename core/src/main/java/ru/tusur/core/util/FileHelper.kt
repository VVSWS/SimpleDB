package ru.tusur.core.util

import android.content.Context
import android.net.Uri
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
     * Creates a fresh empty database file.
     * Deletes old DB + WAL/SHM before creating a new one.
     */
    fun createEmptyDatabaseFile(context: Context) {
        val dbFile = getActiveDatabaseFile(context)

        // Ensure directory exists
        dbFile.parentFile?.mkdirs()

        // Delete old DB + WAL/SHM
        if (dbFile.exists()) dbFile.delete()
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()

        // Create new empty file
        dbFile.createNewFile()
    }

    /**
     * Deletes the active DB file and its WAL/SHM companions.
     */
    fun deleteActiveDatabaseFile(context: Context) {
        val dbFile = getActiveDatabaseFile(context)

        dbFile.delete()
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()
    }

    /**
     * Copies a content:// URI (from SAF or file picker) into a temporary file
     * inside the app's cache directory. Returns the resulting File.
     */
    @Throws(IOException::class)
    fun copyUriToTempFile(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver

        val tempFile = File.createTempFile("import_", ".db", context.cacheDir)

        contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("Unable to open input stream for URI: $uri")

        return tempFile
    }

    fun writeToUri(context: Context, uri: Uri, sourceFile: File) {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            sourceFile.inputStream().use { input ->
                input.copyTo(output)
            }
        }
    }

    /**
     * Ensure a DB name starts with BD_ and has .db extension.
     */
    fun normalizeDbName(rawName: String): String {
        val base = rawName
            .removeSuffix(".db")
            .removePrefix(DB_PREFIX)

        return "${DB_PREFIX}${base}.db"
    }

    fun databaseExists(context: Context): Boolean {
        val file = getActiveDatabaseFile(context)
        return file.exists() && file.length() > 100
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
