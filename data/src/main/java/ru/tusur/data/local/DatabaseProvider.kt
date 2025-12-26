package ru.tusur.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import ru.tusur.data.local.database.AppDatabase
import java.io.File

class DatabaseProvider(private val context: Context) {

    private var currentDatabase: AppDatabase? = null
    private var currentDbFile: File? = null

    fun getDatabase(dbFile: File): AppDatabase {
        if (currentDbFile?.absolutePath != dbFile.absolutePath || currentDatabase == null) {

            currentDatabase?.close()

            if (dbFile.exists() && dbFile.length() < 100) {
                dbFile.delete()
            }

            currentDatabase = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                dbFile.absolutePath
            )
                .fallbackToDestructiveMigration(true)   // ← FIXED
                .openHelperFactory(FrameworkSQLiteOpenHelperFactory())
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


