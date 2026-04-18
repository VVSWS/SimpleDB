package ru.tusur.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Year

// ---------------------------------------------------------
// Интерфейс репозитория для работы со справочными данными
// ---------------------------------------------------------
// Определяет контракт для операций со справочниками:
// - Годы выпуска (Year)
// - Марки автомобилей (Brand)
// - Модели автомобилей (Model)
// - Местоположения (Location)
// Реализуется в слое данных (DefaultReferenceDataRepository)
interface ReferenceDataRepository {

    // ---------------------------------------------------------
    // ГОДЫ (YEARS)
    // ---------------------------------------------------------

    // Получение всех годов в виде реактивного потока
    fun getYears(): Flow<List<Year>>

    // Добавление нового года (возвращает Result для обработки ошибок)
    suspend fun addYear(year: Year): Result<Unit>

    // Удаление года из справочника
    suspend fun deleteYear(year: Year)

    // ---------------------------------------------------------
    // МАРКИ (BRANDS)
    // ---------------------------------------------------------

    // Получение всех марок в виде реактивного потока
    fun getBrands(): Flow<List<Brand>>

    // Добавление новой марки (возвращает Result для обработки ошибок)
    suspend fun addBrand(brand: Brand): Result<Unit>

    // Удаление марки из справочника
    suspend fun deleteBrand(brand: Brand)

    // ---------------------------------------------------------
    // МОДЕЛИ (MODELS)
    // ---------------------------------------------------------

    // Получение всех моделей в виде реактивного потока
    fun getModels(): Flow<List<Model>>

    // Получение моделей, отфильтрованных по марке и году (реактивный поток)
    fun getModelsForBrandAndYear(brand: Brand, year: Year): Flow<List<Model>>

    // Добавление новой модели (возвращает Result для обработки ошибок)
    suspend fun addModel(model: Model): Result<Unit>

    // Удаление модели из справочника
    suspend fun deleteModel(model: Model)

    // ---------------------------------------------------------
    // МЕСТОПОЛОЖЕНИЯ (LOCATIONS)
    // ---------------------------------------------------------

    // Получение всех местоположений в виде реактивного потока
    fun getLocations(): Flow<List<Location>>

    // Добавление нового местоположения (возвращает Result для обработки ошибок)
    suspend fun addLocation(location: Location): Result<Unit>

    // Удаление местоположения из справочника
    suspend fun deleteLocation(location: Location)
}