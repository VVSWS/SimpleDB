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

// ---------------------------------------------------------
// Главный класс базы данных Room
// ---------------------------------------------------------
// Определяет структуру базы данных: таблицы, версию, конвертеры типов
// Абстрактный класс, расширяющий RoomDatabase
// Room автоматически генерирует реализацию во время компиляции
@Database(
    entities = [
        // Таблица записей о неисправностях (основная)
        EntryEntity::class,
        // Справочник годов выпуска
        YearEntity::class,
        // Справочник марок автомобилей
        BrandEntity::class,
        // Справочник моделей автомобилей (связь с маркой и годом)
        ModelEntity::class,
        // Справочник местоположений
        LocationEntity::class,
        // Связующая таблица для изображений (многие-ко-многим)
        EntryImageEntity::class
    ],
    version = 2,           // Текущая версия схемы БД (для миграций)
    exportSchema = false   // Отключение экспорта схемы в JSON (для уменьшения размера)
)
// ---------------------------------------------------------
// Регистрация кастомных конвертеров типов
// ---------------------------------------------------------
// Converters содержит методы для преобразования между типами Kotlin и SQLite
// (например, List<String> ↔ String, Date ↔ Long)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // ---------------------------------------------------------
    // DAO для работы с записями о неисправностях
    // ---------------------------------------------------------
    abstract fun entryDao(): EntryDao

    // ---------------------------------------------------------
    // DAO для работы со справочником годов
    // ---------------------------------------------------------
    abstract fun yearDao(): YearDao

    // ---------------------------------------------------------
    // DAO для работы со справочником марок
    // ---------------------------------------------------------
    abstract fun brandDao(): BrandDao

    // ---------------------------------------------------------
    // DAO для работы со справочником моделей
    // ---------------------------------------------------------
    abstract fun modelDao(): ModelDao

    // ---------------------------------------------------------
    // DAO для работы со справочником местоположений
    // ---------------------------------------------------------
    abstract fun locationDao(): LocationDao

    // ---------------------------------------------------------
    // DAO для работы со связями записей и изображений
    // ---------------------------------------------------------
    abstract fun entryImageDao(): EntryImageDao
}