package ru.tusur.data.mapper

import ru.tusur.data.local.entity.EntryEntity
import ru.tusur.data.local.entity.EntryImageEntity
import ru.tusur.data.local.entity.EntryWithImages
import ru.tusur.domain.export.ExportEntry
import ru.tusur.domain.model.EntryWithImages as DomainEntryWithImages

// ---------------------------------------------------------
// Мапперы для преобразования между Room-сущностями и экспортными DTO
// ---------------------------------------------------------
// Используются при экспорте/импорте базы данных в JSON
// Преобразуют сложные объекты с отношениями в плоские DTO и обратно

// ---------------------------------------------------------
// Room EntryWithImages → ExportEntry (для экспорта)
// ---------------------------------------------------------
// Преобразует запись с изображениями из Room в экспортируемый формат
// Справочные данные берутся как примитивы из EntryEntity (денормализованные поля)
fun EntryWithImages.toExport(): ExportEntry {
    val e = entry  // Room-сущность записи

    return ExportEntry(
        id = e.id,
        timestamp = e.timestamp,

        // Справочные данные (простые примитивы)
        year = e.year,
        brand = e.brand,
        modelName = e.modelName,
        location = e.location,

        // Содержимое записи
        title = e.title,
        description = e.description,

        // Только URI изображений (сами файлы экспортируются отдельно)
        images = images.map { it.uri }
    )
}

// ---------------------------------------------------------
// Доменный EntryWithImages → ExportEntry (для экспорта)
// ---------------------------------------------------------
// Преобразует запись из доменной модели в экспортируемый формат
// Доменные модели содержат объекты Year, Brand, Model, Location - их нужно развернуть
fun DomainEntryWithImages.toExport(): ExportEntry {
    val e = entry  // Доменная модель записи

    return ExportEntry(
        id = e.id,
        timestamp = e.timestamp,

        // Извлечение примитивных значений из объектов справочников
        year = e.year?.value,           // Year → Int
        brand = e.brand?.name,          // Brand → String
        modelName = e.model?.name,      // Model → String (только название)
        location = e.location?.name,    // Location → String

        // Содержимое записи
        title = e.title,
        description = e.description,

        // Список URI изображений (строки)
        images = imageUris
    )
}

// ---------------------------------------------------------
// ExportEntry → Room EntryEntity (без изображений)
// ---------------------------------------------------------
// Преобразует экспортный DTO обратно в Room-сущность
// Изображения восстанавливаются отдельно через toImageEntities()
fun ExportEntry.toEntity(): EntryEntity {
    return EntryEntity(
        id = id,
        timestamp = timestamp,

        // Справочные данные (простые примитивы)
        year = year,
        brand = brand,
        modelName = modelName,
        modelBrand = brand,   // Требуется схемой Room (дублирование марки)
        modelYear = year,     // Требуется схемой Room (дублирование года)
        location = location,

        // Содержимое записи
        title = title,
        description = description
    )
}

// ---------------------------------------------------------
// ExportEntry → список EntryImageEntity
// ---------------------------------------------------------
// Создаёт сущности для связующей таблицы "запись-изображение"
// Используется при импорте/слиянии для восстановления связей
fun ExportEntry.toImageEntities(): List<EntryImageEntity> {
    return images.map { uri ->
        EntryImageEntity(
            entryId = id,    // ID записи (сохраняется из экспорта)
            uri = uri        // URI изображения
        )
    }
}