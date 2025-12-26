package ru.tusur.data.mapper

import ru.tusur.data.local.entity.EntryEntity
import ru.tusur.data.local.entity.EntryWithImages
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.model.Year
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Location

class EntryMapper {

    fun toEntity(domain: FaultEntry): EntryEntity {
        return EntryEntity(
            id = domain.id,
            year = domain.year.value,
            model = domain.model.name,
            location = domain.location.name,
            title = domain.title,
            description = domain.description,
            timestamp = domain.timestamp,
            audioPath = null // or domain.audioPath if you add it later
        )
    }


    fun toDomain(entity: EntryEntity): FaultEntry {
        return FaultEntry(
            id = entity.id,
            year = Year(entity.year),
            model = Model(entity.model),
            location = Location(entity.location),
            title = entity.title,
            description = entity.description,
            timestamp = entity.timestamp
        )
    }

    fun toDomain(entity: EntryWithImages): FaultEntry {
        return FaultEntry(
            id = entity.entry.id,
            year = Year(entity.entry.year),
            model = Model(entity.entry.model),
            location = Location(entity.entry.location),
            title = entity.entry.title,
            description = entity.entry.description,
            timestamp = entity.entry.timestamp
        )
    }

    fun toRecording(entity: EntryWithImages): EntryWithRecording {
        return EntryWithRecording(
            id = entity.entry.id,
            title = entity.entry.title,
            year = Year(entity.entry.year),
            model = Model(entity.entry.model),
            location = Location(entity.entry.location),
            date = entity.entry.timestamp,
            audioPath = entity.entry.audioPath,   // ensure EntryEntity has this field
            description = entity.entry.description

        )
    }
}
