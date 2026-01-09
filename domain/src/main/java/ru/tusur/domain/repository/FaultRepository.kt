package ru.tusur.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Year

interface FaultRepository {
    fun getAllEntries(): Flow<List<FaultEntry>>
    suspend fun getEntryById(id: Long): FaultEntry?
    suspend fun getRecentEntries(limit: Int = 5): List<FaultEntry>

    suspend fun searchEntries(
        year: Int? = null,
        brand: String? = null,
        model: String? = null,
        location: String? = null
    ): List<FaultEntry>

    suspend fun createEntry(entry: FaultEntry): Long
    suspend fun updateEntry(entry: FaultEntry)
    suspend fun deleteEntry(entry: FaultEntry)
    suspend fun saveImages(entryId: Long, uris: List<String>)
    suspend fun getEntryWithRecording(id: Long): EntryWithRecording
    suspend fun deleteImage(path: String)
    suspend fun getEntryCount(): Int


}
