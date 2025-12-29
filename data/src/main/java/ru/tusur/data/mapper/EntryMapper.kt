package ru.tusur.data.mapper

import ru.tusur.data.local.entity.EntryEntity
import ru.tusur.data.local.entity.EntryWithImages
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.model.Year
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Brand

class EntryMapper {

    fun toEntity(domain: FaultEntry): EntryEntity {
        return EntryEntity(
            id = domain.id,
            year = domain.year?.value ?: 0,
            brand = domain.brand?.name ?: "",
            modelName = domain.model?.name,
            modelBrand = domain.model?.brand?.name,
            modelYear = domain.model?.year?.value,
            location = domain.location?.name,
            title = domain.title,
            description = domain.description,
            timestamp = domain.timestamp
        )
    }

    fun toDomain(entity: EntryEntity): FaultEntry {
        val model = if (
            entity.modelName != null &&
            entity.modelBrand != null &&
            entity.modelYear != null
        ) {
            Model(
                name = entity.modelName,
                brand = Brand(entity.modelBrand),
                year = Year(entity.modelYear)
            )
        } else null

        return FaultEntry(
            id = entity.id,
            year = entity.year?.let { Year(it) },
            brand = entity.brand?.let { Brand(it) },
            model = model,
            location = entity.location?.let { Location(it) },
            title = entity.title,
            description = entity.description,
            timestamp = entity.timestamp
        )
    }

    fun toDomain(entity: EntryWithImages): FaultEntry {
        val e = entity.entry

        val model = if (
            e.modelName != null &&
            e.modelBrand != null &&
            e.modelYear != null
        ) {
            Model(
                name = e.modelName,
                brand = Brand(e.modelBrand),
                year = Year(e.modelYear)
            )
        } else null

        return FaultEntry(
            id = e.id,
            year = e.year?.let { Year(it) },
            brand = e.brand?.let { Brand(it) },
            model = model,
            location = e.location?.let { Location(it) },
            title = e.title,
            description = e.description,
            timestamp = e.timestamp,
            imageUris = entity.images.map { it.uri }
        )
    }

    fun toRecording(entity: EntryWithImages): EntryWithRecording {
        val e = entity.entry

        val model = if (
            e.modelName != null &&
            e.modelBrand != null &&
            e.modelYear != null
        ) {
            Model(
                name = e.modelName,
                brand = Brand(e.modelBrand),
                year = Year(e.modelYear)
            )
        } else null

        return EntryWithRecording(
            id = e.id,
            title = e.title,
            year = e.year?.let { Year(it) },
            brand = e.brand?.let { Brand(it) },
            model = model,
            location = e.location?.let { Location(it) },
            timestamp = e.timestamp,
            description = e.description,
            imageUris = entity.images.map { it.uri }
        )
    }
}