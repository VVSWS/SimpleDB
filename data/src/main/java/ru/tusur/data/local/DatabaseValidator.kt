package ru.tusur.data.local

import android.database.sqlite.SQLiteDatabase
import ru.tusur.domain.repository.DatabaseValidator as DomainDatabaseValidator
import ru.tusur.core.util.FileHelper
import java.io.File

class RoomDatabaseValidator : DomainDatabaseValidator {

    companion object {
        private const val REQUIRED_TABLES = listOf(
            "entries", "years", "models", "locations", "entry_images"
        )
    }

    override suspend fun validateDatabase(dbFile: File): DomainDatabaseValidator.ValidationResult {
        if (!dbFile.exists()) {
            return DomainDatabaseValidator.ValidationResult.Error("File does not exist")
        }

        if (!FileHelper.isSQLiteFile(dbFile)) {
            return DomainDatabaseValidator.ValidationResult.Error("Not a valid SQLite database")
        }

        return try {
            SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            ).use { db ->
                val userVersion = db.version
                if (userVersion != 1) {
                    return DomainDatabaseValidator.ValidationResult.Error("Incompatible schema version: $userVersion")
                }

                val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
                val tables = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    tables.add(cursor.getString(0))
                }
                cursor.close()

                val missing = REQUIRED_TABLES - tables
                if (missing.isNotEmpty()) {
                    DomainDatabaseValidator.ValidationResult.Error("Missing tables: ${missing.joinToString()}")
                } else {
                    DomainDatabaseValidator.ValidationResult.Success
                }
            }
        } catch (e: Exception) {
            DomainDatabaseValidator.ValidationResult.Error("Database validation failed: ${e.message}")
        }
    }
}