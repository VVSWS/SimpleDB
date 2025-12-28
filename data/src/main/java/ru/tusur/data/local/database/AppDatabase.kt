package ru.tusur.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.tusur.data.local.database.dao.BrandDao
import ru.tusur.data.local.database.dao.EntryDao
import ru.tusur.data.local.database.dao.EntryImageDao
import ru.tusur.data.local.database.dao.LocationDao
import ru.tusur.data.local.database.dao.ModelDao
import ru.tusur.data.local.database.dao.YearDao
import ru.tusur.data.local.entity.Converters
import ru.tusur.data.local.entity.EntryEntity
import ru.tusur.data.local.entity.EntryImageEntity
import ru.tusur.data.local.entity.LocationEntity
import ru.tusur.data.local.entity.ModelEntity
import ru.tusur.data.local.entity.YearEntity
import ru.tusur.data.local.entity.BrandEntity


@Database(
    entities = [
        EntryEntity::class,
        YearEntity::class,
        BrandEntity::class,
        ModelEntity::class,
        LocationEntity::class,
        EntryImageEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun yearDao(): YearDao
    abstract fun brandDao(): BrandDao
    abstract fun modelDao(): ModelDao
    abstract fun locationDao(): LocationDao
    abstract fun entryImageDao(): EntryImageDao
}