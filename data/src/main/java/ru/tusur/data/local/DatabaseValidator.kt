package ru.tusur.data.local

import android.database.sqlite.SQLiteDatabase
import ru.tusur.stop.core.util.FileHelper

class DatabaseValidator {

    companion object {
        private const val REQUIRED_TABLES = listOf(
            "entries", "years", "models", "locations", "entry_images"
        )
    }

    fun validateDatabase(dbFile: java.io.File): ValidationResult {
        if (!dbFile.exists()) {
            return ValidationResult.Error("File does not exist")
        }

        if (!FileHelper.isSQLiteFile(dbFile)) {
            return ValidationResult.Error("Not a valid SQLite database")
        }

        return try {
            SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            ).use { db ->
                val userVersion = db.version
                if (userVersion != 1) {
                    return ValidationResult.Error("Incompatible schema version: $userVersion")
                }

                val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
                val tables = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    tables.add(cursor.getString(0))
                }
                cursor.close()

                val missing = REQUIRED_TABLES - tables
                if (missing.isNotEmpty()) {
                    ValidationResult.Error("Missing tables: ${missing.joinToString()}")
                } else {
                    ValidationResult.Success
                }
            }
        } catch (e: Exception) {
            ValidationResult.Error("Database validation failed: ${e.message}")
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}