package ru.tusur.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.tusur.stop.domain.model.Location
import ru.tusur.stop.domain.model.Model
import ru.tusur.stop.domain.model.Year

interface ReferenceDataRepository {
    fun getYears(): Flow<List<Year>>
    suspend fun addYear(year: Year): Result<Unit>
    fun getModels(): Flow<List<Model>>
    suspend fun addModel(model: Model): Result<Unit>
    fun getLocations(): Flow<List<Location>>
    suspend fun addLocation(location: Location): Result<Unit>
}