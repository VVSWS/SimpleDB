package ru.tusur.data.mapper

import ru.tusur.data.local.entity.EntryEntity
import ru.tusur.data.local.entity.EntryWithRelations
import ru.tusur.data.local.entity.ModelEntity
import ru.tusur.domain.model.*
import ru.tusur.data.local.entity.EntryWithImages as RoomEntryWithImages

class EntryMapper {

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    private fun modelFromEntity(model: ModelEntity?): Model? =
        model?.let {
            Model(
                name = it.name,
                brand = Brand(it.brandName),
                year = Year(it.yearValue)
            )
        }

    private fun modelFromComposite(
        name: String?,
        brand: String?,
        year: Int?
    ): Model? =
        if (name != null && brand != null && year != null) {
            Model(
                name = name,
                brand = Brand(brand),
                year = Year(year)
            )
        } else null

    // ---------------------------------------------------------
    // DOMAIN → ENTITY
    // ---------------------------------------------------------

    fun toEntity(domain: FaultEntry): EntryEntity {
        return EntryEntity(
            id = domain.id,
            year = domain.year?.value,
            brand = domain.brand?.name,

            modelName = domain.model?.name,
            modelBrand = domain.model?.brand?.name,
            modelYear = domain.model?.year?.value,

            location = domain.location?.name,
            title = domain.title,
            description = domain.description,
            timestamp = domain.timestamp
        )
    }

    // ---------------------------------------------------------
    // ENTITY → DOMAIN (simple)
    // ---------------------------------------------------------

    fun toDomain(entity: EntryEntity): FaultEntry {
        return FaultEntry(
            id = entity.id,
            year = entity.year?.let { Year(it) },
            brand = entity.brand?.let { Brand(it) },
            model = modelFromComposite(entity.modelName, entity.modelBrand, entity.modelYear),
            location = entity.location?.let { Location(it) },
            title = entity.title,
            description = entity.description,
            timestamp = entity.timestamp
        )
    }

    // ---------------------------------------------------------
    // ENTITY → DOMAIN (with images)
    // ---------------------------------------------------------

    fun toDomain(entity: EntryEntity, imageUris: List<String>): FaultEntry {
        return FaultEntry(
            id = entity.id,
            year = entity.year?.let { Year(it) },
            brand = entity.brand?.let { Brand(it) },
            model = modelFromComposite(entity.modelName, entity.modelBrand, entity.modelYear),
            location = entity.location?.let { Location(it) },
            title = entity.title,
            description = entity.description,
            timestamp = entity.timestamp,
            imageUris = imageUris
        )
    }

    // ---------------------------------------------------------
    // ENTRY WITH RELATIONS → DOMAIN
    // (used for lists, search results)
    // ---------------------------------------------------------

    fun fromRelations(rel: EntryWithRelations, model: ModelEntity?): FaultEntry {
        val e = rel.entry

        return FaultEntry(
            id = e.id,
            year = rel.year?.value?.let { Year(it) },
            brand = rel.brand?.name?.let { Brand(it) },
            model = modelFromEntity(model),
            location = rel.location?.name?.let { Location(it) },
            title = e.title,
            description = e.description,
            timestamp = e.timestamp
        )
    }

    // ---------------------------------------------------------
    // ENTRY WITH IMAGES → DOMAIN
    // (used for detail screen)
    // ---------------------------------------------------------

    fun fromImages(rel: RoomEntryWithImages, model: ModelEntity?): FaultEntry {
        val e = rel.entry

        return FaultEntry(
            id = e.id,
            year = e.year?.let { Year(it) },
            brand = e.brand?.let { Brand(it) },
            model = modelFromEntity(model),
            location = e.location?.let { Location(it) },
            title = e.title,
            description = e.description,
            timestamp = e.timestamp,
            imageUris = rel.images.map { it.uri }
        )
    }

    // ---------------------------------------------------------
    // ENTRY WITH IMAGES → DOMAIN EntryWithImages wrapper
    // (used by recording screen)
    // ---------------------------------------------------------

    fun toEntryWithImages(rel: RoomEntryWithImages, model: ModelEntity?): EntryWithImages {
        val e = rel.entry
        val uris = rel.images.map { it.uri }

        return EntryWithImages(
            entry = FaultEntry(
                id = e.id,
                year = e.year?.let { Year(it) },
                brand = e.brand?.let { Brand(it) },
                model = modelFromEntity(model),
                location = e.location?.let { Location(it) },
                title = e.title,
                description = e.description,
                timestamp = e.timestamp,
                imageUris = uris
            ),
            imageUris = uris
        )
    }

    // ---------------------------------------------------------
    // ENTRY WITH IMAGES → ENTRY WITH RECORDING
    // ---------------------------------------------------------

    fun toRecording(entity: EntryWithImages, model: ModelEntity?): EntryWithRecording {
        val e = entity.entry

        return EntryWithRecording(
            id = e.id,
            title = e.title,
            year = e.year,
            brand = e.brand,
            model = modelFromEntity(model),
            location = e.location,
            timestamp = e.timestamp,
            description = e.description,
            imageUris = entity.imageUris
        )
    }
}
