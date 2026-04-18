package ru.tusur.data.repository

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.tusur.core.files.ImageStorage
import ru.tusur.data.local.DatabaseProvider
import ru.tusur.data.local.entity.EntryImageEntity
import ru.tusur.data.mapper.EntryMapper
import ru.tusur.domain.model.EntryWithImages
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

// ---------------------------------------------------------
// Реализация репозитория для работы с записями о неисправностях
// ---------------------------------------------------------
// Обеспечивает все операции CRUD для записей и связанных изображений
// Управляет транзакциями БД и физическим удалением файлов изображений
class DefaultFaultRepository(
    private val appContext: Context,           // Контекст для доступа к filesDir (удаление изображений)
    private val provider: DatabaseProvider,    // Провайдер для получения текущего экземпляра БД
    private val mapper: EntryMapper            // Маппер для преобразования между Room и доменом
) : FaultRepository {

    // ---------------------------------------------------------
    // DAO-доступ через текущую базу данных
    // ---------------------------------------------------------
    // Геттеры лениво получают актуальные DAO из текущего экземпляра БД
    private val entryDao get() = provider.getCurrentDatabase().entryDao()
    private val imageDao get() = provider.getCurrentDatabase().entryImageDao()

    // ---------------------------------------------------------
    // ЗАПРОСЫ ЗАПИСЕЙ
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // Получение всех записей в виде реактивного потока
    // ---------------------------------------------------------
    // Загружает записи со связанными справочниками и преобразует в доменные модели
    override fun getAllEntries(): Flow<List<FaultEntry>> {
        return entryDao.getAllEntries().map { list ->
            list.map { rel ->
                // Получение модели автомобиля по составному ключу
                val model = entryDao.getModelForEntry(
                    rel.entry.modelName,
                    rel.entry.modelBrand,
                    rel.entry.modelYear
                )
                // Преобразование в доменную модель
                mapper.fromRelations(rel, model)
            }
        }
    }

    // ---------------------------------------------------------
    // Получение записи по ID (с изображениями)
    // ---------------------------------------------------------
    override suspend fun getEntryById(id: Long): FaultEntry? {
        val rel = entryDao.getEntryById(id) ?: return null

        val model = entryDao.getModelForEntry(
            rel.entry.modelName,
            rel.entry.modelBrand,
            rel.entry.modelYear
        )

        return mapper.fromImages(rel, model)
    }

    // ---------------------------------------------------------
    // Получение последних записей (ограниченное количество)
    // ---------------------------------------------------------
    override suspend fun getRecentEntries(limit: Int): List<FaultEntry> {
        return entryDao.getRecentEntries().map { entity ->
            mapper.toDomain(entity)
        }
    }

    // ---------------------------------------------------------
    // Поиск записей по фильтрам
    // ---------------------------------------------------------
    override suspend fun searchEntries(
        year: Int?,
        brand: String?,
        model: String?,
        location: String?
    ): List<FaultEntry> {
        return entryDao.searchEntries(year, brand, model, location).map { entity ->
            mapper.toDomain(entity)
        }
    }

    // ---------------------------------------------------------
    // CRUD ОПЕРАЦИИ
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // Создание новой записи
    // ---------------------------------------------------------
    // Выполняется в транзакции: вставка записи + вставка всех изображений
    override suspend fun createEntry(entry: FaultEntry): Long {
        val db = provider.getCurrentDatabase()

        return db.withTransaction {
            // Вставка записи и получение сгенерированного ID
            val id = entryDao.insertEntry(mapper.toEntity(entry))

            // Вставка всех изображений, связанных с записью
            entry.imageUris.forEach { uri ->
                imageDao.insertImage(
                    EntryImageEntity(
                        entryId = id,
                        uri = uri
                    )
                )
            }

            id
        }
    }

    // ---------------------------------------------------------
    // Обновление существующей записи
    // ---------------------------------------------------------
    // Атомарно заменяет все изображения записи (удаляет старые, вставляет новые)
    override suspend fun updateEntry(entry: FaultEntry) {
        // Обновление основных полей записи
        entryDao.updateEntry(mapper.toEntity(entry))

        // Атомарная замена изображений
        imageDao.deleteImagesForEntry(entry.id)  // Удаление всех старых связей
        entry.imageUris.forEach { uri ->
            imageDao.insertImage(EntryImageEntity(entryId = entry.id, uri = uri))  // Вставка новых
        }
    }

    // ---------------------------------------------------------
    // Удаление записи
    // ---------------------------------------------------------
    // Транзакция: удаление связей изображений → удаление записи
    // После транзакции - физическое удаление файлов изображений
    override suspend fun deleteEntry(entry: FaultEntry) {
        val db = provider.getCurrentDatabase()

        db.withTransaction {
            imageDao.deleteImagesForEntry(entry.id)  // Удаление связей из БД
            entryDao.deleteEntryById(entry.id)       // Удаление самой записи
        }

        // Физическое удаление файлов изображений с диска
        entry.imageUris.forEach { uri ->
            ImageStorage.deleteImageFile(appContext, uri)
        }
    }

    // ---------------------------------------------------------
    // Получение общего количества записей
    // ---------------------------------------------------------
    override suspend fun getEntryCount(): Int {
        return entryDao.getEntryCount()
    }

    // ---------------------------------------------------------
    // ОПЕРАЦИИ С ИЗОБРАЖЕНИЯМИ
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // Добавление изображения к записи
    // ---------------------------------------------------------
    override suspend fun addImageToEntry(entryId: Long, uri: String) {
        imageDao.insertImage(EntryImageEntity(entryId = entryId, uri = uri))
    }

    // ---------------------------------------------------------
    // Удаление изображения из записи
    // ---------------------------------------------------------
    // Удаляет связь в БД и физический файл изображения
    override suspend fun removeImageFromEntry(entryId: Long, uri: String) {
        imageDao.deleteImage(entryId, uri)          // Удаление связи
        ImageStorage.deleteImageFile(appContext, uri)  // Удаление файла
    }

    // ---------------------------------------------------------
    // Удаление изображения по URI (из всех записей)
    // ---------------------------------------------------------
    override suspend fun deleteImage(uri: String) {
        ImageStorage.deleteImageFile(appContext, uri)  // Удаление файла
    }

    // ---------------------------------------------------------
    // Получение всех записей с их изображениями
    // ---------------------------------------------------------
    // Используется при экспорте базы данных
    override suspend fun getEntriesWithImages(): List<EntryWithImages> {
        return entryDao.getAllEntriesWithImages().map { rel ->
            val model = entryDao.getModelForEntry(
                rel.entry.modelName,
                rel.entry.modelBrand,
                rel.entry.modelYear
            )
            mapper.toEntryWithImages(rel, model)
        }
    }

    // ---------------------------------------------------------
    // ОПЕРАЦИИ ДЛЯ ЭКРАНА АУДИОЗАПИСИ
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // Получение записи со всеми данными для воспроизведения
    // ---------------------------------------------------------
    override suspend fun getEntryWithRecording(id: Long): EntryWithRecording {
        val rel = entryDao.getEntryWithRecording(id)

        val model = entryDao.getModelForEntry(
            rel.entry.modelName,
            rel.entry.modelBrand,
            rel.entry.modelYear
        )

        // Преобразование Room EntryWithImages → доменный EntryWithImages
        val domainEntryWithImages = mapper.toEntryWithImages(rel, model)

        // Преобразование в специализированную модель для аудиоплеера
        return mapper.toRecording(domainEntryWithImages, model)
    }

    // ---------------------------------------------------------
    // СБРОС БАЗЫ ДАННЫХ (только записи и изображения)
    // ---------------------------------------------------------
    // Удаляет все записи, все связи изображений и физические файлы
    // Справочные данные (марки, модели, года, локации) НЕ удаляются
    override suspend fun resetDatabase() {
        // 1. Удаление всех записей из БД
        entryDao.deleteAllEntries()

        // 2. Удаление всех связей изображений из БД
        imageDao.deleteAllImages()

        // 3. Физическое удаление всех файлов изображений с диска
        ImageStorage.clearAllImages(appContext.filesDir)
    }
}