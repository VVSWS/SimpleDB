package ru.tusur.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.tusur.domain.model.EntryWithImages
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.domain.model.FaultEntry

interface FaultRepository {

    // ---------------------------------------------------------
    // Entry queries
    // ---------------------------------------------------------
    fun getAllEntries(): Flow<List<FaultEntry>>
    suspend fun getEntryById(id: Long): FaultEntry?
    suspend fun getRecentEntries(limit: Int = 5): List<FaultEntry>

    suspend fun searchEntries(
        year: Int? = null,
        brand: String? = null,
        model: String? = null,
        location: String? = null
    ): List<FaultEntry>

    // ---------------------------------------------------------
    // Entry CRUD
    // ---------------------------------------------------------
    suspend fun createEntry(entry: FaultEntry): Long
    suspend fun updateEntry(entry: FaultEntry)
    suspend fun deleteEntry(entry: FaultEntry)
    suspend fun getEntryCount(): Int

    // ---------------------------------------------------------
    // Images
    // ---------------------------------------------------------
    suspend fun addImageToEntry(entryId: Long, uri: String)
    suspend fun removeImageFromEntry(entryId: Long, uri: String)
    suspend fun deleteImage(uri: String)

    // Used by export
    suspend fun getEntriesWithImages(): List<EntryWithImages>

    // Used by recording screen
    suspend fun getEntryWithRecording(id: Long): EntryWithRecording
}
