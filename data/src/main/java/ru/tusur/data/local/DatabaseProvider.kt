package ru.tusur.data.local

import android.content.Context
import androidx.room.Room
import ru.tusur.core.util.FileHelper
import ru.tusur.data.local.database.AppDatabase
import java.io.File

class DatabaseProvider(
    private val context: Context
) {

    @Volatile
    private var currentDatabase: AppDatabase? = null

    private var currentDbFile: File? = null

    fun initializeDatabase() {
        val dbFile = FileHelper.getActiveDatabaseFile(context)
        currentDbFile = dbFile

        currentDatabase = if (dbFile.exists()) {
            // If there is an existing DB file, load from it
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "SimpleDB.db"
            )

                .createFromFile(dbFile)
                .fallbackToDestructiveMigration(true)
                .build()
        } else {
            // If no file yet, create a fresh Room DB at this location
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "SimpleDB.db" // ← FIXED HERE
            )
                .fallbackToDestructiveMigration(true)
                .build()
        }
    }

    fun resetDatabase() {
        currentDatabase?.close()
        currentDatabase = null
    }

    fun getCurrentDatabase(): AppDatabase {
        // Lazy safety: if someone calls without init, initialize now
        if (currentDatabase == null) {
            initializeDatabase()
        }
        return currentDatabase
            ?: throw IllegalStateException("Database initialization failed")
    }

    fun getActiveDatabaseFile(): File {
        if (currentDbFile == null) {
            currentDbFile = FileHelper.getActiveDatabaseFile(context)
        }
        return currentDbFile!!
    }

}
