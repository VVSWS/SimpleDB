package ru.tusur.data.local

import android.content.Context
import androidx.room.Room
import ru.tusur.data.local.database.AppDatabase
import java.io.File

class DatabaseProvider(private val context: Context) {

    private var currentDatabase: AppDatabase? = null
    private var currentDbFile: File? = null

    fun getDatabase(dbFile: File): AppDatabase {
        if (currentDbFile != dbFile || currentDatabase == null) {
            currentDatabase?.close()
            currentDatabase = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                dbFile.name
            )
                .createFromFile(dbFile)
                .fallbackToDestructiveMigration()
                .build()

            currentDbFile = dbFile
        }
        return currentDatabase!!
    }

    fun closeDatabase() {
        currentDatabase?.close()
        currentDatabase = null
        currentDbFile = null
    }
}
