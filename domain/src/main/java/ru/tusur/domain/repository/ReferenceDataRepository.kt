package ru.tusur.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Year

interface ReferenceDataRepository {

    // YEARS
    fun getYears(): Flow<List<Year>>
    suspend fun addYear(year: Year): Result<Unit>
    suspend fun deleteYear(year: Year)

    // BRANDS
    fun getBrands(): Flow<List<Brand>>
    suspend fun addBrand(brand: Brand): Result<Unit>
    suspend fun deleteBrand(brand: Brand)

    // MODELS
    fun getModels(): Flow<List<Model>>
    fun getModelsForBrandAndYear(brand: Brand, year: Year): Flow<List<Model>>
    suspend fun addModel(model: Model): Result<Unit>
    suspend fun deleteModel(model: Model)

    // LOCATIONS
    fun getLocations(): Flow<List<Location>>
    suspend fun addLocation(location: Location): Result<Unit>
    suspend fun deleteLocation(location: Location)
}
