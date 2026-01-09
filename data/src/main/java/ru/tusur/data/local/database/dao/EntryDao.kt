package ru.tusur.data.local.database.dao

import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.EntryEntity
import ru.tusur.data.local.entity.EntryWithImages
import ru.tusur.data.local.entity.EntryWithRelations
import ru.tusur.data.local.entity.ModelEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface EntryDao {

    // OVERVIEW LIST
    @Transaction
    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<EntryWithRelations>>

    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryById(id: Long): EntryWithImages?

    @Query("SELECT * FROM entries ORDER BY timestamp DESC LIMIT 5")
    suspend fun getRecentEntries(): List<EntryEntity>

    @Query("SELECT * FROM entries")
    fun getAllSync(): List<EntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EntryEntity): Long

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: Long): EntryEntity?

    @Delete
    suspend fun delete(entry: EntryEntity)

    @Query("""
        SELECT * FROM entries
        WHERE (:year IS NULL OR year = :year)
          AND (:brand IS NULL OR brand = :brand)
          AND (:modelName IS NULL OR modelName = :modelName)
          AND (:location IS NULL OR location = :location)
        ORDER BY timestamp DESC
    """)
    suspend fun searchEntries(
        year: Int?,
        brand: String?,
        modelName: String?,
        location: String?
    ): List<EntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: EntryEntity): Long

    @Update
    suspend fun updateEntry(entry: EntryEntity)

    @Query("SELECT COUNT(*) FROM entries")
    suspend fun getEntryCount(): Int

    @Delete
    suspend fun deleteEntry(entry: EntryEntity)

    @Query("SELECT id FROM entries")
    suspend fun getAllIds(): List<Long>

    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryWithRecording(id: Long): EntryWithImages

    @Transaction
    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    suspend fun getAllEntriesWithImages(): List<EntryWithImages>


    // ---------------------------------------------------------
    // ⭐ REQUIRED FOR ROOM 2.8.4 — manual composite model lookup
    // ---------------------------------------------------------
    @Query("""
        SELECT * FROM models
        WHERE name = :modelName
          AND brandName = :modelBrand
          AND yearValue = :modelYear
        LIMIT 1
    """)
    suspend fun getModelForEntry(
        modelName: String?,
        modelBrand: String?,
        modelYear: Int?
    ): ModelEntity?
}
