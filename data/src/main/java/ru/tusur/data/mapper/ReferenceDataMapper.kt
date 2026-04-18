package ru.tusur.data.mapper

import ru.tusur.data.local.entity.BrandEntity
import ru.tusur.data.local.entity.LocationEntity
import ru.tusur.data.local.entity.ModelEntity
import ru.tusur.data.local.entity.YearEntity
import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Year

// ---------------------------------------------------------
// Маппер для преобразования справочных данных
// ---------------------------------------------------------
// Обеспечивает двустороннее преобразование между Room-сущностями и доменными моделями
// Для каждого типа справочника (марка, год, модель, локация) определены методы toDomain и toEntity
class ReferenceDataMapper {

    // ---------------------------------------------------------
    // МАРКИ (Brand)
    // ---------------------------------------------------------

    // Преобразование Room-сущности в доменную модель марки
    fun toDomain(entity: BrandEntity) = Brand(entity.name)

    // Преобразование доменной модели марки в Room-сущность
    fun toEntity(domain: Brand) = BrandEntity(domain.name)

    // ---------------------------------------------------------
    // ГОДЫ (Year)
    // ---------------------------------------------------------

    // Преобразование Room-сущности в доменную модель года
    fun toDomain(entity: YearEntity) = Year(entity.value)

    // Преобразование доменной модели года в Room-сущность
    fun toEntity(domain: Year) = YearEntity(domain.value)

    // ---------------------------------------------------------
    // МОДЕЛИ (Model)
    // ---------------------------------------------------------

    // Преобразование Room-сущности в доменную модель модели
    // Требует разворачивания brandName и yearValue в объекты Brand и Year
    fun toDomain(entity: ModelEntity) = Model(
        name = entity.name,
        brand = Brand(entity.brandName),
        year = Year(entity.yearValue)
    )

    // Преобразование доменной модели модели в Room-сущность
    // Извлекает примитивные значения из объектов Brand и Year
    fun toEntity(domain: Model) = ModelEntity(
        name = domain.name,
        brandName = domain.brand.name,
        yearValue = domain.year.value
    )

    // ---------------------------------------------------------
    // МЕСТОПОЛОЖЕНИЯ (Location)
    // ---------------------------------------------------------

    // Преобразование Room-сущности в доменную модель локации
    fun toDomain(entity: LocationEntity) = Location(entity.name)

    // Преобразование доменной модели локации в Room-сущность
    fun toEntity(domain: Location) = LocationEntity(domain.name)
}