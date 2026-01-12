package ru.tusur.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.tusur.data.local.DatabaseProvider
import ru.tusur.data.mapper.ReferenceDataMapper
import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Year
import ru.tusur.domain.repository.ReferenceDataRepository

class DefaultReferenceDataRepository(
    private val provider: DatabaseProvider,
    private val mapper: ReferenceDataMapper
) : ReferenceDataRepository {

    private val yearDao get() = provider.getCurrentDatabase().yearDao()
    private val brandDao get() = provider.getCurrentDatabase().brandDao()
    private val modelDao get() = provider.getCurrentDatabase().modelDao()
    private val locationDao get() = provider.getCurrentDatabase().locationDao()

    // -------------------------
    // YEARS
    // -------------------------

    override fun getYears(): Flow<List<Year>> =
        yearDao.getAllYears().map { list ->
            list.map(mapper::toDomain)
        }

    override suspend fun addYear(year: Year): Result<Unit> = try {
        val entity = mapper.toEntity(year)
        val id = yearDao.insertYear(entity)

        if (id == -1L)
            Result.failure(IllegalStateException("Year ${year.value} already exists"))
        else
            Result.success(Unit)

    } catch (e: Exception) {
        Result.failure(e)
    }

    // -------------------------
    // BRANDS
    // -------------------------

    override fun getBrands(): Flow<List<Brand>> =
        brandDao.getAllBrands().map { list ->
            list.map(mapper::toDomain)
        }

    override suspend fun addBrand(brand: Brand): Result<Unit> = try {
        val entity = mapper.toEntity(brand)
        val id = brandDao.insertBrand(entity)

        if (id == -1L)
            Result.failure(IllegalStateException("Brand '${brand.name}' already exists"))
        else
            Result.success(Unit)

    } catch (e: Exception) {
        Result.failure(e)
    }

    // -------------------------
    // MODELS
    // -------------------------

    override fun getModels(): Flow<List<Model>> =
        modelDao.getAllModels().map { list ->
            list.map(mapper::toDomain)
        }

    override fun getModelsForBrandAndYear(
        brand: Brand,
        year: Year
    ): Flow<List<Model>> =
        modelDao.getModelsForBrandAndYear(
            brandName = brand.name,
            yearValue = year.value
        ).map { list ->
            list.map(mapper::toDomain)
        }

    override suspend fun addModel(model: Model): Result<Unit> = try {
        val entity = mapper.toEntity(model)
        modelDao.insertModel(entity)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // -------------------------
    // LOCATIONS
    // -------------------------

    override fun getLocations(): Flow<List<Location>> =
        locationDao.getAllLocations().map { list ->
            list.map(mapper::toDomain)
        }

    override suspend fun addLocation(location: Location): Result<Unit> = try {
        val entity = mapper.toEntity(location)
        val id = locationDao.insertLocation(entity)

        if (id == -1L)
            Result.failure(IllegalStateException("Location '${location.name}' already exists"))
        else
            Result.success(Unit)

    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteYear(year: Year) {
        yearDao.deleteYear(mapper.toEntity(year))
    }

    override suspend fun deleteBrand(brand: Brand) {
        brandDao.deleteBrand(mapper.toEntity(brand))
    }

    override suspend fun deleteModel(model: Model) {
        modelDao.deleteModel(mapper.toEntity(model))
    }

    override suspend fun deleteLocation(location: Location) {
        locationDao.deleteLocation(mapper.toEntity(location))
    }
}
