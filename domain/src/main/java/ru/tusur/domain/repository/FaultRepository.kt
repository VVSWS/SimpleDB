package ru.tusur.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.tusur.domain.model.EntryWithImages
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.domain.model.FaultEntry

// ---------------------------------------------------------
// Интерфейс репозитория для работы с записями о неисправностях
// ---------------------------------------------------------
// Определяет контракт для всех операций с записями и связанными изображениями
// Реализуется в слое данных (DefaultFaultRepository)
// Обеспечивает чистую архитектуру: доменный слой не зависит от конкретной реализации
interface FaultRepository {

    // ---------------------------------------------------------
    // ЗАПРОСЫ ЗАПИСЕЙ
    // ---------------------------------------------------------

    // Получение всех записей в виде реактивного потока (Flow)
    fun getAllEntries(): Flow<List<FaultEntry>>

    // Получение записи по ID (с изображениями)
    suspend fun getEntryById(id: Long): FaultEntry?

    // Получение последних записей (ограниченное количество, по умолчанию 5)
    suspend fun getRecentEntries(limit: Int = 5): List<FaultEntry>

    // Поиск записей по фильтрам (все параметры опциональны)
    suspend fun searchEntries(
        year: Int? = null,
        brand: String? = null,
        model: String? = null,
        location: String? = null
    ): List<FaultEntry>

    // ---------------------------------------------------------
    // CRUD ОПЕРАЦИИ
    // ---------------------------------------------------------

    // Создание новой записи (возвращает сгенерированный ID)
    suspend fun createEntry(entry: FaultEntry): Long

    // Обновление существующей записи
    suspend fun updateEntry(entry: FaultEntry)

    // Удаление записи (также удаляет связанные изображения)
    suspend fun deleteEntry(entry: FaultEntry)

    // Получение общего количества записей
    suspend fun getEntryCount(): Int

    // ---------------------------------------------------------
    // ОПЕРАЦИИ С ИЗОБРАЖЕНИЯМИ
    // ---------------------------------------------------------

    // Добавление изображения к записи
    suspend fun addImageToEntry(entryId: Long, uri: String)

    // Удаление изображения из записи (также удаляет физический файл)
    suspend fun removeImageFromEntry(entryId: Long, uri: String)

    // Удаление изображения по URI (из всех записей)
    suspend fun deleteImage(uri: String)

    // ---------------------------------------------------------
    // СПЕЦИАЛИЗИРОВАННЫЕ ЗАПРОСЫ
    // ---------------------------------------------------------

    // Получение всех записей с их изображениями (для экспорта)
    suspend fun getEntriesWithImages(): List<EntryWithImages>

    // Получение записи для экрана аудиозаписи (оптимизированная структура)
    suspend fun getEntryWithRecording(id: Long): EntryWithRecording

    // ---------------------------------------------------------
    // ОПЕРАЦИИ ОБСЛУЖИВАНИЯ
    // ---------------------------------------------------------

    // Сброс базы данных (удаление всех записей и изображений)
    suspend fun resetDatabase()
}