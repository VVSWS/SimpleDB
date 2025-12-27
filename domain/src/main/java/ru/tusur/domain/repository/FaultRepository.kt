package ru.tusur.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.domain.model.FaultEntry

interface FaultRepository {
    fun getAllEntries(): Flow<List<FaultEntry>>
    suspend fun getEntryById(id: Long): FaultEntry?
    suspend fun getRecentEntries(limit: Int = 5): List<FaultEntry>
    suspend fun searchEntries(
        year: String? = null,
        model: String? = null,
        location: String? = null
    ): List<FaultEntry>
    suspend fun createEntry(entry: FaultEntry): Long
    suspend fun updateEntry(entry: FaultEntry)
    suspend fun deleteEntry(entry: FaultEntry)
    suspend fun getEntryWithRecording(id: Long): EntryWithRecording
}