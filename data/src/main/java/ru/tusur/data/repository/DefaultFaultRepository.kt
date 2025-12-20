package ru.tusur.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.tusur.stop.data.local.database.dao.EntryDao
import ru.tusur.data.mapper.EntryMapper
import ru.tusur.stop.domain.model.FaultEntry
import ru.tusur.stop.domain.repository.FaultRepository

class DefaultFaultRepository(
    private val entryDao: EntryDao,
    private val mapper: EntryMapper
) : FaultRepository {

    override fun getAllEntries(): Flow<List<FaultEntry>> {
        return entryDao.getAllEntries().map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }

    override suspend fun getEntryById(id: Long): FaultEntry? {
        return entryDao.getEntryById(id)?.let { mapper.toDomain(it) }
    }

    override suspend fun getRecentEntries(limit: Int): List<FaultEntry> {
        return entryDao.getRecentEntries().map { mapper.toDomain(it) }
    }

    override suspend fun searchEntries(
        year: Int?,
        model: String?,
        location: String?
    ): List<FaultEntry> {
        return entryDao.searchEntries(year, model, location)
            .map { mapper.toDomain(it) }
    }

    override suspend fun createEntry(entry: FaultEntry): Long {
        return entryDao.insertEntry(mapper.toEntity(entry))
    }

    override suspend fun updateEntry(entry: FaultEntry) {
        entryDao.updateEntry(mapper.toEntity(entry))
    }

    override suspend fun deleteEntry(entry: FaultEntry) {
        entryDao.deleteEntry(mapper.toEntity(entry))
    }
}