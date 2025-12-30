package ru.tusur.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.tusur.data.local.database.dao.EntryDao
import ru.tusur.data.local.database.dao.EntryImageDao
import ru.tusur.data.local.entity.EntryImageEntity
import ru.tusur.data.mapper.EntryMapper
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

class DefaultFaultRepository(
    private val entryDao: EntryDao,
    private val imageDao: EntryImageDao,
    private val mapper: EntryMapper
) : FaultRepository {

    override fun getAllEntries(): Flow<List<FaultEntry>> {
        return entryDao.getAllEntries().map { list ->
            list.map { rel ->
                val model = entryDao.getModelForEntry(
                    rel.entry.modelName,
                    rel.entry.modelBrand,
                    rel.entry.modelYear
                )
                mapper.fromRelations(rel, model)
            }
        }
    }

    override suspend fun getEntryById(id: Long): FaultEntry? {
        val rel = entryDao.getEntryById(id) ?: return null

        val model = entryDao.getModelForEntry(
            rel.entry.modelName,
            rel.entry.modelBrand,
            rel.entry.modelYear
        )

        return mapper.fromImages(rel, model)
    }

    override suspend fun getRecentEntries(limit: Int): List<FaultEntry> {
        return entryDao.getRecentEntries().map { entity ->
            val model = entryDao.getModelForEntry(
                entity.modelName,
                entity.modelBrand,
                entity.modelYear
            )
            mapper.toDomain(entity)   // ← FIXED
        }
    }

    override suspend fun searchEntries(
        year: Int?,
        brand: String?,
        model: String?,
        location: String?
    ): List<FaultEntry> {
        return entryDao.searchEntries(year, brand, model, location).map { entity ->
            val modelEntity = entryDao.getModelForEntry(
                entity.modelName,
                entity.modelBrand,
                entity.modelYear
            )
            mapper.toDomain(entity)   // ← FIXED
        }
    }

    override suspend fun createEntry(entry: FaultEntry): Long {
        val id = entryDao.insertEntry(mapper.toEntity(entry))
        saveImages(id, entry.imageUris)
        return id
    }

    override suspend fun updateEntry(entry: FaultEntry) {
        entryDao.updateEntry(mapper.toEntity(entry))
        saveImages(entry.id, entry.imageUris)
    }

    override suspend fun saveImages(entryId: Long, uris: List<String>) {
        imageDao.deleteImagesForEntry(entryId)
        uris.forEach { uri ->
            imageDao.insertImage(EntryImageEntity(entryId = entryId, uri = uri))
        }
    }

    override suspend fun getEntryWithRecording(id: Long): EntryWithRecording {
        val rel = entryDao.getEntryWithRecording(id)

        val model = entryDao.getModelForEntry(
            rel.entry.modelName,
            rel.entry.modelBrand,
            rel.entry.modelYear
        )

        return mapper.toRecording(rel, model)
    }

    override suspend fun deleteEntry(entry: FaultEntry) {
        imageDao.deleteImagesForEntry(entry.id)
        entryDao.deleteEntry(mapper.toEntity(entry))
    }
}
