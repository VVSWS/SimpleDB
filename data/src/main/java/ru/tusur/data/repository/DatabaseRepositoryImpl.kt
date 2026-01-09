package ru.tusur.data.repository

import ru.tusur.data.local.DatabaseProvider
import ru.tusur.domain.repository.DatabaseRepository
import java.io.File

class DatabaseRepositoryImpl(
    private val provider: DatabaseProvider
) : DatabaseRepository {

    override suspend fun mergeDatabase(externalFile: File): Int {
        val mainDb = provider.getCurrentDatabase()
        val externalDb = provider.openExternalDatabase(externalFile)

        return try {
            // ⭐ Correct method from your DAO
            val externalEntries = externalDb.entryDao().getAllSync()

            // Insert into main DB
            externalEntries.forEach { entry ->
                mainDb.entryDao().insertEntry(entry)
            }

            externalEntries.size
        } finally {
            externalDb.close()
        }
    }
}
