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

// ---------------------------------------------------------
// Реализация репозитория для работы со справочными данными
// ---------------------------------------------------------
// Обеспечивает доступ к справочникам: годы, марки, модели, местоположения
// Поддерживает получение (реактивные потоки), добавление и удаление записей
class DefaultReferenceDataRepository(
    private val provider: DatabaseProvider,    // Провайдер для получения текущего экземпляра БД
    private val mapper: ReferenceDataMapper    // Маппер для преобразования между Room и доменом
) : ReferenceDataRepository {

    // ---------------------------------------------------------
    // DAO-доступ через текущую базу данных
    // ---------------------------------------------------------
    // Геттеры лениво получают актуальные DAO из текущего экземпляра БД
    private val yearDao get() = provider.getCurrentDatabase().yearDao()
    private val brandDao get() = provider.getCurrentDatabase().brandDao()
    private val modelDao get() = provider.getCurrentDatabase().modelDao()
    private val locationDao get() = provider.getCurrentDatabase().locationDao()

    // ---------------------------------------------------------
    // ГОДЫ (YEARS)
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // Получение всех годов в виде реактивного потока
    // ---------------------------------------------------------
    override fun getYears(): Flow<List<Year>> =
        yearDao.getAllYears().map { list ->
            list.map(mapper::toDomain)  // Преобразование Room-сущностей в доменные модели
        }

    // ---------------------------------------------------------
    // Добавление нового года
    // ---------------------------------------------------------
    // При успешной вставке возвращает Result.success
    // Если год уже существует (id == -1L) - возвращает ошибку
    override suspend fun addYear(year: Year): Result<Unit> = try {
        val entity = mapper.toEntity(year)  // Преобразование в Room-сущность
        val id = yearDao.insertYear(entity)  // Вставка в БД

        // Проверка: если id == -1L, запись не была вставлена (конфликт первичного ключа)
        if (id == -1L)
            Result.failure(IllegalStateException("Year ${year.value} already exists"))
        else
            Result.success(Unit)

    } catch (e: Exception) {
        Result.failure(e)
    }

    // ---------------------------------------------------------
    // МАРКИ (BRANDS)
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // Получение всех марок в виде реактивного потока
    // ---------------------------------------------------------
    override fun getBrands(): Flow<List<Brand>> =
        brandDao.getAllBrands().map { list ->
            list.map(mapper::toDomain)
        }

    // ---------------------------------------------------------
    // Добавление новой марки
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // МОДЕЛИ (MODELS)
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // Получение всех моделей в виде реактивного потока
    // ---------------------------------------------------------
    override fun getModels(): Flow<List<Model>> =
        modelDao.getAllModels().map { list ->
            list.map(mapper::toDomain)
        }

    // ---------------------------------------------------------
    // Получение моделей, отфильтрованных по марке и году
    // ---------------------------------------------------------
    // Используется для динамических списков в форме создания записи
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

    // ---------------------------------------------------------
    // Добавление новой модели
    // ---------------------------------------------------------
    // Для моделей не требуется проверка на дубликат
    // Room IGNORE или REPLACE обработает конфликт автоматически
    override suspend fun addModel(model: Model): Result<Unit> = try {
        val entity = mapper.toEntity(model)
        modelDao.insertModel(entity)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ---------------------------------------------------------
    // МЕСТОПОЛОЖЕНИЯ (LOCATIONS)
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // Получение всех местоположений в виде реактивного потока
    // ---------------------------------------------------------
    override fun getLocations(): Flow<List<Location>> =
        locationDao.getAllLocations().map { list ->
            list.map(mapper::toDomain)
        }

    // ---------------------------------------------------------
    // Добавление нового местоположения
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // УДАЛЕНИЕ СПРАВОЧНЫХ ДАННЫХ
    // ---------------------------------------------------------

    // Удаление года из справочника
    override suspend fun deleteYear(year: Year) {
        yearDao.deleteYear(mapper.toEntity(year))
    }

    // Удаление марки из справочника
    override suspend fun deleteBrand(brand: Brand) {
        brandDao.deleteBrand(mapper.toEntity(brand))
    }

    // Удаление модели из справочника
    override suspend fun deleteModel(model: Model) {
        modelDao.deleteModel(mapper.toEntity(model))
    }

    // Удаление местоположения из справочника
    override suspend fun deleteLocation(location: Location) {
        locationDao.deleteLocation(mapper.toEntity(location))
    }
}