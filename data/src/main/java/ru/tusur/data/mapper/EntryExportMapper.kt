package ru.tusur.data.mapper

import ru.tusur.data.local.entity.EntryEntity
import ru.tusur.data.local.entity.EntryImageEntity
import ru.tusur.data.local.entity.EntryWithImages
import ru.tusur.domain.export.ExportEntry
import ru.tusur.domain.model.EntryWithImages as DomainEntryWithImages

/**
 * Convert Room EntryWithImages → JSON export model.
 * Uses raw primitive dictionary values stored in EntryEntity.
 */
fun EntryWithImages.toExport(): ExportEntry {
    val e = entry

    return ExportEntry(
        id = e.id,
        timestamp = e.timestamp,

        // Dictionary values (raw primitives)
        year = e.year,
        brand = e.brand,
        modelName = e.modelName,
        location = e.location,

        // Entry content
        title = e.title,
        description = e.description,

        // Only URIs, files exported separately
        images = images.map { it.uri }
    )
}

/**
 * Convert DOMAIN EntryWithImages → JSON export model.
 * Domain models wrap dictionary values, so unwrap them.
 */
fun DomainEntryWithImages.toExport(): ExportEntry {
    val e = entry

    return ExportEntry(
        id = e.id,
        timestamp = e.timestamp,

        year = e.year?.value,
        brand = e.brand?.name,
        modelName = e.model?.name,
        location = e.location?.name,

        title = e.title,
        description = e.description,

        images = imageUris
    )
}

/**
 * Convert JSON entry → Room EntryEntity (no images).
 * Images are restored separately using toImageEntities().
 */
fun ExportEntry.toEntity(): EntryEntity {
    return EntryEntity(
        id = id,
        timestamp = timestamp,

        // Dictionary values (raw primitives)
        year = year,
        brand = brand,
        modelName = modelName,
        modelBrand = brand,   // required by your Room schema
        modelYear = year,     // required by your Room schema
        location = location,

        // Entry content
        title = title,
        description = description
    )
}

/**
 * Convert JSON entry → list of EntryImageEntity.
 * Used during import/merge to restore image rows.
 */
fun ExportEntry.toImageEntities(): List<EntryImageEntity> {
    return images.map { uri ->
        EntryImageEntity(
            entryId = id,
            uri = uri
        )
    }
}
