package ru.tusur.data.mapper

import ru.tusur.data.local.entity.BrandEntity
import ru.tusur.data.local.entity.LocationEntity
import ru.tusur.data.local.entity.ModelEntity
import ru.tusur.data.local.entity.YearEntity
import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Year

class ReferenceDataMapper {

    // BRAND
    fun toDomain(entity: BrandEntity) = Brand(entity.name)
    fun toEntity(domain: Brand) = BrandEntity(domain.name)

    // YEAR
    fun toDomain(entity: YearEntity) = Year(entity.value)
    fun toEntity(domain: Year) = YearEntity(domain.value)

    // MODEL
    fun toDomain(entity: ModelEntity) = Model(
        name = entity.name,
        brand = Brand(entity.brandName),
        year = Year(entity.yearValue)
    )

    fun toEntity(domain: Model) = ModelEntity(
        name = domain.name,
        brandName = domain.brand.name,
        yearValue = domain.year.value
    )

    // LOCATION
    fun toDomain(entity: LocationEntity) = Location(entity.name)
    fun toEntity(domain: Location) = LocationEntity(domain.name)
}
