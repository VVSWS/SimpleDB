package ru.tusur.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.EntryEntity
import ru.tusur.data.local.entity.EntryWithImages

@Dao
interface EntryDao {

    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<EntryEntity>>

    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryById(id: Long): EntryWithImages?

    @Query("SELECT * FROM entries ORDER BY timestamp DESC LIMIT 5")
    suspend fun getRecentEntries(): List<EntryEntity>

    @Query("SELECT * FROM entries")
    fun getAllSync(): List<EntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: Long): EntryEntity?

    @Delete
    suspend fun delete(entry: EntryEntity)

    @Query("""
        SELECT * FROM entries
        WHERE (:year IS NULL OR year = :year)
          AND (:brand IS NULL OR brand = :brand)
          AND (:model IS NULL OR model = :model)
          AND (:location IS NULL OR location = :location)
        ORDER BY timestamp DESC
    """)
    suspend fun searchEntries(
        year: Int?,
        brand: String?,
        model: String?,
        location: String?
    ): List<EntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: EntryEntity): Long

    @Update
    suspend fun updateEntry(entry: EntryEntity)

    @Delete
    suspend fun deleteEntry(entry: EntryEntity)

    // NEW — this replaces your broken "Any" version
    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryWithRecording(id: Long): EntryWithImages
}
