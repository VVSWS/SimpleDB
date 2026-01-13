package ru.tusur.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.EntryEntity
import ru.tusur.data.local.entity.EntryWithImages
import ru.tusur.data.local.entity.EntryWithRelations
import ru.tusur.data.local.entity.ModelEntity

@Dao
interface EntryDao {

    // ---------------------------------------------------------
    // LISTS
    // ---------------------------------------------------------

    @Transaction
    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<EntryWithRelations>>

    @Query("SELECT * FROM entries ORDER BY timestamp DESC LIMIT 5")
    suspend fun getRecentEntries(): List<EntryEntity>

    @Query("SELECT * FROM entries")
    fun getAllSync(): List<EntryEntity>

    // ---------------------------------------------------------
    // SINGLE ENTRY
    // ---------------------------------------------------------

    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryById(id: Long): EntryWithImages?

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: Long): EntryEntity?

    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryWithRecording(id: Long): EntryWithImages

    // ---------------------------------------------------------
    // SEARCH
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // CRUD
    // ---------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: EntryEntity): Long

    @Update
    suspend fun updateEntry(entry: EntryEntity)

    @Delete
    suspend fun deleteEntry(entry: EntryEntity)

    @Query("SELECT COUNT(*) FROM entries")
    suspend fun getEntryCount(): Int

    @Query("SELECT id FROM entries")
    suspend fun getAllIds(): List<Long>

    // ---------------------------------------------------------
    // IMAGES (joined)
    // ---------------------------------------------------------

    @Transaction
    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    suspend fun getAllEntriesWithImages(): List<EntryWithImages>

    // ---------------------------------------------------------
    // MODEL LOOKUP (composite key)
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

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)


    @Query("DELETE FROM entries")
    suspend fun deleteAllEntries()

}
