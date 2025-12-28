package ru.tusur.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Year

interface ReferenceDataRepository {
    fun getYears(): Flow<List<Year>>
    suspend fun addYear(year: Year): Result<Unit>
    fun getBrands(): Flow<List<Brand>>
    suspend fun addBrand(model: Brand): Result<Unit>
    fun getModels(): Flow<List<Model>>
    suspend fun addModel(model: Model): Result<Unit>
    fun getLocations(): Flow<List<Location>>
    suspend fun addLocation(location: Location): Result<Unit>
}