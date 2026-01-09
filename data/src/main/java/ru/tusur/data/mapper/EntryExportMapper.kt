package ru.tusur.data.mapper

import ru.tusur.data.local.entity.EntryEntity
import ru.tusur.data.local.entity.EntryImageEntity
import ru.tusur.data.local.entity.EntryWithImages
import ru.tusur.domain.export.ExportEntry

/**
 * Convert DB entry + images → JSON export model.
 * Images are exported as URIs only (actual files are copied separately).
 */
fun EntryWithImages.toExport(): ExportEntry {
    return ExportEntry(
        id = entry.id,
        timestamp = entry.timestamp,
        year = entry.year,
        brand = entry.brand,
        modelName = entry.modelName,
        modelBrand = entry.modelBrand,
        modelYear = entry.modelYear,
        location = entry.location,
        title = entry.title,
        description = entry.description,
        notes = entry.notes,
        images = images.map { it.uri }   // only URIs, not binary data
    )
}

/**
 * Convert JSON entry → DB entry (without images).
 * Images are restored separately using toImageEntities().
 */
fun ExportEntry.toEntity(): EntryEntity {
    return EntryEntity(
        id = id,
        timestamp = timestamp,
        year = year,
        brand = brand,
        modelName = modelName,
        modelBrand = modelBrand,
        modelYear = modelYear,
        location = location,
        title = title,
        description = description,
        notes = notes
    )
}

/**
 * Convert JSON entry → list of EntryImageEntity.
 * This is used during import/merge to restore image rows.
 */
fun ExportEntry.toImageEntities(): List<EntryImageEntity> {
    return images.map { uri ->
        EntryImageEntity(
            entryId = id,
            uri = uri
        )
    }
}
