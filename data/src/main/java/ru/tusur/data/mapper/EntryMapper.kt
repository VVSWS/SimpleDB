package ru.tusur.data.mapper

import ru.tusur.data.local.entity.EntryEntity
import ru.tusur.data.local.entity.EntryWithImages
import ru.tusur.stop.domain.model.FaultEntry

class EntryMapper {

    fun toEntity(domain: FaultEntry): EntryEntity {
        return EntryEntity(
            id = domain.id,
            year = domain.year.value,
            model = domain.model.name,
            location = domain.location.name,
            title = domain.title,
            description = domain.description,
            timestamp = domain.timestamp
        )
    }

    fun toDomain(entity: EntryEntity): FaultEntry {
        return FaultEntry(
            id = entity.id,
            year = ru.tusur.stop.domain.model.Year(entity.year),
            model = ru.tusur.stop.domain.model.Model(entity.model),
            location = ru.tusur.stop.domain.model.Location(entity.location),
            title = entity.title,
            description = entity.description,
            timestamp = entity.timestamp
        )
    }

    fun toDomain(entity: EntryWithImages): FaultEntry {
        return toDomain(entity.entry).copy(
            imageUris = entity.images.map { it.uri }
        )
    }
}