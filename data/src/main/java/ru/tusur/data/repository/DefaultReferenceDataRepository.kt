package ru.tusur.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.tusur.stop.data.local.database.dao.LocationDao
import ru.tusur.stop.data.local.database.dao.ModelDao
import ru.tusur.stop.data.local.database.dao.YearDao
import ru.tusur.data.mapper.ReferenceDataMapper
import ru.tusur.stop.domain.model.Location
import ru.tusur.stop.domain.model.Model
import ru.tusur.stop.domain.model.Year
import ru.tusur.stop.domain.repository.ReferenceDataRepository

class DefaultReferenceDataRepository(
    private val yearDao: YearDao,
    private val modelDao: ModelDao,
    private val locationDao: LocationDao,
    private val mapper: ReferenceDataMapper
) : ReferenceDataRepository {

    override fun getYears(): Flow<List<Year>> {
        return yearDao.getAllYears().map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }

    override suspend fun addYear(year: Year): Result<Unit> {
        return try {
            val entity = mapper.toEntity(year)
            val id = yearDao.insertYear(entity)
            if (id == -1L) {
                Result.failure(IllegalStateException("Year ${year.value} already exists"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getModels(): Flow<List<Model>> {
        return modelDao.getAllModels().map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }

    override suspend fun addModel(model: Model): Result<Unit> {
        return try {
            val entity = mapper.toEntity(model)
            val id = modelDao.insertModel(entity)
            if (id == -1L) {
                Result.failure(IllegalStateException("Model '${model.name}' already exists"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLocations(): Flow<List<Location>> {
        return locationDao.getAllLocations().map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }

    override suspend fun addLocation(location: Location): Result<Unit> {
        return try {
            val entity = mapper.toEntity(location)
            val id = locationDao.insertLocation(entity)
            if (id == -1L) {
                Result.failure(IllegalStateException("Location '${location.name}' already exists"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}