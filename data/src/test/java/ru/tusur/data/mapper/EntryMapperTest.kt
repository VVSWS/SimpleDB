package ru.tusur.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import ru.tusur.data.local.entity.*
import ru.tusur.domain.model.*
import ru.tusur.data.local.entity.EntryEntity
import ru.tusur.data.local.entity.EntryImageEntity
import ru.tusur.data.local.entity.EntryWithImages
import ru.tusur.data.local.entity.EntryWithRelations
import ru.tusur.data.local.entity.ModelEntity


class EntryMapperTest {

    private lateinit var mapper: EntryMapper

    @Before
    fun setup() {
        mapper = EntryMapper()
    }

    // ---------------------------------------------------------
    // Тест 1: DOMAIN -> ENTITY
    // Сценарий: Создание записи о неисправности двигателя Toyota Camry
    // ---------------------------------------------------------
    @Test
    fun `toEntity maps car repair record correctly`() {
        // Arrange (Данные из приложения)
        val carModel = Model(
            name = "Camry",
            brand = Brand("Toyota"),
            year = Year(2021)
        )

        val repairRecord = FaultEntry(
            id = 101L,
            title = "Пропуски зажигания в первом цилиндре",
            description = "Неисправен модуль зажигания.",
            timestamp = 1715600000L,
            year = Year(2021),       // Год выпуска авто
            brand = Brand("Toyota"), // Бренд авто
            model = carModel,        // Модель авто
            location = Location("Двигатель"),
            imageUris = listOf()
        )

        // Act (Преобразование в Entity для базы данных)
        val entity = mapper.toEntity(repairRecord)

        // Assert (Проверка полей)
        assertEquals(101L, entity.id)
        assertEquals(2021, entity.year) // Год авто
        assertEquals("Toyota", entity.brand) // Бренд авто

        // Проверка разбитых полей модели
        assertEquals("Camry", entity.modelName)
        assertEquals("Toyota", entity.modelBrand)
        assertEquals(2021, entity.modelYear)

        assertEquals("Двигатель", entity.location)
        assertEquals("Пропуски зажигания в первом цилиндре", entity.title)
        assertEquals("Неисправен модуль зажигания.", entity.description)
    }

    // ---------------------------------------------------------
    // Тест 2: ENTITY -> DOMAIN (Simple)
    // Сценарий: Создание записи о проблеме с тормозами BMW
    // ---------------------------------------------------------
    @Test
    fun `toDomain restores car repair record from database`() {
        // Arrange (Данные из базы)
        val entity = EntryEntity(
            id = 202L,
            year = 2019,
            brand = "BMW",
            modelName = "X5",
            modelBrand = "BMW",
            modelYear = 2019,
            location = "Передний мост",
            title = "Скрип передних тормозов",
            description = "Требуется замена колодок и диагностика дисков.",
            timestamp = 1715700000L
        )

        // Act
        val domain = mapper.toDomain(entity)

        // Assert
        assertEquals(202L, domain.id)
        assertEquals(2019, domain.year?.value)
        assertEquals("BMW", domain.brand?.name)

        // Проверка сборки модели
        assertEquals("X5", domain.model?.name)
        assertEquals("BMW", domain.model?.brand?.name)
        assertEquals(2019, domain.model?.year?.value)

        assertEquals("Передний мост", domain.location?.name)
        assertEquals("Скрип передних тормозов", domain.title)
    }

    // ---------------------------------------------------------
    // Тест 3: ENTITY + IMAGES -> DOMAIN
    // Сценарий: Создание записи с фотографиями повреждения провода
    // ---------------------------------------------------------
    @Test
    fun `toDomain with images restores photos of car damage`() {
        // Arrange
        val entity = EntryEntity(
            id = 303L,
            year = null, // Данные могут быть неполными
            brand = null,
            modelName = null,
            modelBrand = null,
            modelYear = null,
            location = null,
            title = "Перетерт жгут проводов двери",
            description = "Механическое повреждение",
            timestamp = 1715800000L
        )

        // Список URI фотографий вмятины
        val photoUris = listOf(
            "file:///storage/emulated/0/AutoService/dent_left_door.jpg",
            "file:///storage/emulated/0/AutoService/dent_close_up.jpg"
        )

        // Act
        val domain = mapper.toDomain(entity, photoUris)

        // Assert
        assertEquals(303L, domain.id)
        assertNull(domain.year) // Убедимся, что null обработан корректно
        assertEquals("Перетерт жгут проводов двери", domain.title)

        // Проверка фотографий
        assertEquals(2, domain.imageUris.size)
        assertEquals("file:///storage/emulated/0/AutoService/dent_left_door.jpg", domain.imageUris[0])
        assertEquals("file:///storage/emulated/0/AutoService/dent_close_up.jpg", domain.imageUris[1])
    }

    // ---------------------------------------------------------
    // Тест 4: Relations -> Domain
    // Сценарий: Создание записи о неисправном блоке климат-контроля
    // Год, Бренд, Расположение
    // ---------------------------------------------------------
    @Test
    fun `fromRelations maps complex car service data correctly`() {
        // Основная запись
        val entryEntity = EntryEntity(
            id = 404L,
            year = 2015,
            brand = "Kia",
            modelName = null, // Модель придет отдельно через ModelEntity
            modelBrand = null,
            modelYear = null,
            location = "Климат-контроль",
            title = "Неправильная регулировка температуры",
            description = "Неисправен датчик испарителя",
            timestamp = 1715900000L
        )

        // Справочные данные (как будто пришли из JOIN запроса Room)
        val yearRef = YearEntity(2015)
        val brandRef = BrandEntity("Kia")
        val locRef = LocationEntity("Климат-контроль")

        val relations = EntryWithRelations(
            entry = entryEntity,
            year = yearRef,
            brand = brandRef,
            location = locRef
        )

        // Отдельная сущность модели (например, Rio)
        val modelEntity = ModelEntity(
            name = "Rio",
            brandName = "Kia",
            yearValue = 2015
        )

        // Act
        val domain = mapper.fromRelations(relations, modelEntity)

        // Assert
        assertEquals("Неправильная регулировка температуры", domain.title)
        assertEquals("Kia", domain.brand?.name)

        // Самое важное: модель собралась из отдельной сущности
        assertEquals("Rio", domain.model?.name)
        assertEquals("Kia", domain.model?.brand?.name)
        assertEquals(2015, domain.model?.year?.value)
    }

    // ---------------------------------------------------------
    // Тест 5: Images Wrapper -> Domain
    // Сценарий: Создание записи о перегоревшем предохраниетеле
    // с фотоотчетом (EntryWithImages)
    // ---------------------------------------------------------
    @Test
    fun `fromImages maps car repair report with photos`() {
        val entryEntity = EntryEntity(
            id = 505L,
            year = 2023,
            brand = "Mercedes-Benz",
            modelName = "E-Class",
            modelBrand = "Mercedes-Benz",
            modelYear = 2023,
            location = "Межсетевой интерфейс",
            title = "Постоянно работает вентилятор",
            description = "Изност щеток электродвигателя",
            timestamp = 1716000000L
        )

        // Фотографии до и после
        val photoBefore = EntryImageEntity(entryId = 505L, uri = "content://media/photo_before_poly.jpg")
        val photoAfter = EntryImageEntity(entryId = 505L, uri = "content://media/photo_after_poly.jpg")

        val roomEntryWithImages = EntryWithImages(
            entry = entryEntity,
            images = listOf(photoBefore, photoAfter)
        )

        val modelEntity = ModelEntity("E-Class", "Mercedes-Benz", 2023)

        // Act
        val domain = mapper.fromImages(roomEntryWithImages, modelEntity)

        // Assert
        assertEquals("Постоянно работает вентилятор", domain.title)
        assertEquals("Mercedes-Benz", domain.model?.brand?.name)

        // Проверка порядка и содержания фото
        assertEquals(2, domain.imageUris.size)
        assertEquals("content://media/photo_before_poly.jpg", domain.imageUris[0])
        assertEquals("content://media/photo_after_poly.jpg", domain.imageUris[1])
    }

    // ---------------------------------------------------------
    // Тест 6: Обработка некорректных данных (Null Safety)
    // Сценарий: В базе есть название модели, но потерян бренд модели
    // ---------------------------------------------------------
    @Test
    fun `model becomes null if car model brand is missing in database`() {
        val entity = EntryEntity(
            id = 606L,
            year = 2010,
            brand = "Lada", // Бренд автомобиля известен
            modelName = "Vesta",
            modelBrand = null, // Ошибка в данных: бренд модели неизвестен
            modelYear = 2010,
            location = null,
            title = "Проблемы с электроникой",
            description = "Не работает стеклоподъемник",
            timestamp = 1716100000L
        )

        val domain = mapper.toDomain(entity)

        // Логика маппера должна вернуть null для всей модели, если данные неполные
        assertNull(domain.model)

        // Но основная информация о заявке должна сохраниться
        assertEquals("Lada", domain.brand?.name)
        assertEquals("Проблемы с электроникой", domain.title)
    }
}