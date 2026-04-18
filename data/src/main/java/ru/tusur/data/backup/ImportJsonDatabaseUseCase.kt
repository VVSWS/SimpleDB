package ru.tusur.data.backup

import android.content.Context
import kotlinx.serialization.json.Json
import ru.tusur.domain.export.ExportDatabase
import ru.tusur.domain.model.*
import ru.tusur.domain.repository.FaultRepository
import ru.tusur.domain.repository.ReferenceDataRepository
import java.io.File
import java.io.InputStream

// ---------------------------------------------------------
// UseCase для импорта базы данных из JSON-файла
// ---------------------------------------------------------
// Читает экспортированный JSON, создаёт записи и справочные данные
// Восстанавливает изображения из папки backup/images
class ImportJsonDatabaseUseCase(
    private val context: Context,                         // Контекст для доступа к filesDir
    private val faultRepository: FaultRepository,         // Репозиторий для записей о неисправностях
    private val referenceRepository: ReferenceDataRepository  // Репозиторий для справочников
) {

    // ---------------------------------------------------------
    // Оператор invoke - запуск импорта
    // ---------------------------------------------------------
    // inputStream: поток JSON-файла (обычно из выбранного пользователем файла)
    // Возвращает Result<Unit> - успех или ошибка
    suspend operator fun invoke(inputStream: InputStream): Result<Unit> {
        return try {

            // ---------------------------------------------------------
            // 1. Чтение и десериализация JSON
            // ---------------------------------------------------------
            // Чтение всего содержимого потока в строку
            val json = inputStream.bufferedReader().use { it.readText() }
            // Преобразование JSON в объект ExportDatabase
            val export = Json.decodeFromString<ExportDatabase>(json)

            // ---------------------------------------------------------
            // Определение директории с резервными копиями изображений
            // ---------------------------------------------------------
            // Ожидаемая структура: filesDir/backup/images/{oldEntryId}/*.jpg
            val backupDir = File(context.filesDir, "backup/images")

            // ---------------------------------------------------------
            // 2. Вставка записей и справочных значений
            // ---------------------------------------------------------
            export.entries.forEach { dto ->

                // ---------------------------------------------------------
                // Извлечение обязательных полей из DTO
                // ---------------------------------------------------------
                // Если поле отсутствует - выбрасываем исключение (коррупция данных)
                val brandName = dto.brand ?: error("Missing brand")
                val locationName = dto.location ?: error("Missing location")
                val modelName = dto.modelName ?: error("Missing modelName")
                val yearValue = dto.year ?: error("Missing year")

                // ---------------------------------------------------------
                // Создание доменных объектов для справочных данных
                // ---------------------------------------------------------
                val brand = Brand(brandName)          // Марка автомобиля
                val location = Location(locationName)  // Местоположение
                val year = Year(yearValue)             // Год выпуска
                val model = Model(                     // Модель автомобиля
                    name = modelName,
                    brand = brand,
                    year = year
                )

                // ---------------------------------------------------------
                // Вставка справочных данных (дубликаты игнорируются)
                // ---------------------------------------------------------
                referenceRepository.addBrand(brand)
                referenceRepository.addLocation(location)
                referenceRepository.addYear(year)
                referenceRepository.addModel(model)

                // ---------------------------------------------------------
                // Создание записи о неисправности
                // ---------------------------------------------------------
                val entry = FaultEntry(
                    id = 0,                              // 0 = новая запись, ID будет сгенерирован БД
                    timestamp = dto.timestamp,           // Время создания записи
                    year = year,
                    brand = brand,
                    model = model,
                    location = location,
                    title = dto.title,                   // Заголовок
                    description = dto.description,       // Описание проблемы
                    imageUris = emptyList()              // Пока пустой список, изображения добавим позже
                )

                // Вставка записи в БД и получение сгенерированного ID
                val newEntryId = faultRepository.createEntry(entry)

                // ---------------------------------------------------------
                // 3. Копирование изображений из резервной папки в хранилище приложения
                // ---------------------------------------------------------
                // Путь к папке с изображениями для этой записи в бэкапе
                val entryBackupDir = File(backupDir, dto.id.toString())

                // Проверка, существует ли папка с изображениями для данной записи
                if (entryBackupDir.exists()) {

                    // Целевая директория внутри хранилища приложения
                    // Структура: filesDir/images/{newEntryId}/
                    val dstDir = File(context.filesDir, "images/$newEntryId")
                    dstDir.mkdirs()  // Создание директории (включая родительские)

                    // Копирование каждого файла изображения
                    entryBackupDir.listFiles()?.forEach { src ->
                        // Целевой файл с тем же именем
                        val dst = File(dstDir, src.name)
                        // Копирование с перезаписью, если файл уже существует
                        src.copyTo(dst, overwrite = true)

                        // Регистрация изображения в БД (связь с записью)
                        faultRepository.addImageToEntry(
                            entryId = newEntryId,
                            uri = dst.toURI().toString()  // Сохранение URI скопированного файла
                        )
                    }
                }
            }

            // ---------------------------------------------------------
            // Успешное завершение импорта
            // ---------------------------------------------------------
            Result.success(Unit)

        } catch (e: Exception) {
            // ---------------------------------------------------------
            // Обработка ошибки
            // ---------------------------------------------------------
            Result.failure(e)
        }
    }
}