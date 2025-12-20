package ru.tusur.core.util

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object FileHelper {

    fun copyFile(source: File, destination: File): Boolean {
        return try {
            destination.parentFile?.mkdirs()
            FileInputStream(source).use { input ->
                FileOutputStream(destination).use { output ->
                    input.channel.transferTo(0, input.channel.size(), output.channel)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isSQLiteFile(file: File): Boolean {
        return try {
            val magic = ByteArray(16)
            FileInputStream(file).use { it.read(magic) }
            String(magic).startsWith("SQLite format 3\u0000")
        } catch (e: Exception) {
            false
        }
    }

    fun backupFile(file: File): File? {
        val backup = File("${file.absolutePath}.bak")
        return if (copyFile(file, backup)) backup else null
    }

    fun getDatabasePath(context: Context, isExternal: Boolean): File {
        return if (isExternal) {
            File(context.getExternalFilesDir("databases"), "current.db")
        } else {
            File(context.filesDir, "databases/current.db")
        }
    }
}