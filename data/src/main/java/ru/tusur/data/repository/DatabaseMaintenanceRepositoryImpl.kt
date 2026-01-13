package ru.tusur.data.repository

import ru.tusur.domain.repository.DatabaseMaintenanceRepository
import ru.tusur.data.local.database.AppDatabase

class DatabaseMaintenanceRepositoryImpl(
    private val db: AppDatabase
) : DatabaseMaintenanceRepository {

    override suspend fun vacuum() {
        db.openHelper.writableDatabase.execSQL("VACUUM")
    }
}
