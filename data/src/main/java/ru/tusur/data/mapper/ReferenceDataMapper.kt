package ru.tusur.data.mapper

import ru.tusur.data.local.entity.BrandEntity
import ru.tusur.data.local.entity.LocationEntity
import ru.tusur.data.local.entity.ModelEntity
import ru.tusur.data.local.entity.YearEntity
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Year
import ru.tusur.domain.model.Brand

class ReferenceDataMapper {

    fun toEntity(domain: Year): YearEntity = YearEntity(domain.value)
    fun toDomain(entity: YearEntity): Year = Year(entity.value)

    fun toEntity(domain: Brand): BrandEntity = BrandEntity(domain.name)
    fun toDomain(entity: BrandEntity): Brand = Brand(entity.name)

    fun toEntity(domain: Model): ModelEntity = ModelEntity(domain.name)
    fun toDomain(entity: ModelEntity): Model = Model(entity.name)

    fun toEntity(domain: Location): LocationEntity = LocationEntity(domain.name)
    fun toDomain(entity: LocationEntity): Location = Location(entity.name)
}