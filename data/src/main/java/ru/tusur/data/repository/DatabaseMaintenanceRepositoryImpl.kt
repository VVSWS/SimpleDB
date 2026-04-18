package ru.tusur.data.repository

import ru.tusur.domain.repository.DatabaseMaintenanceRepository
import ru.tusur.data.local.database.AppDatabase

// ---------------------------------------------------------
// Реализация репозитория для обслуживания базы данных
// ---------------------------------------------------------
// Предоставляет методы для выполнения операций обслуживания SQLite
// В данном случае - выполнение команды VACUUM для оптимизации БД
class DatabaseMaintenanceRepositoryImpl(
    private val db: AppDatabase   // Экземпляр базы данных Room
) : DatabaseMaintenanceRepository {

    // ---------------------------------------------------------
    // Выполнение команды VACUUM
    // ---------------------------------------------------------
    // VACUUM перестраивает всю базу данных, освобождая неиспользуемое пространство
    // Дефрагментирует файл БД, уменьшая его размер и улучшая производительность
    // Рекомендуется выполнять после массового удаления данных
    override suspend fun vacuum() {
        // Получение writableDatabase через открытый helper Room
        // execSQL выполняет произвольную SQL-команду
        db.openHelper.writableDatabase.execSQL("VACUUM")
    }
}