package ru.tusur.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.YearEntity

@Dao
interface YearDao {

    // Insert or update a year (UI usage)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYear(entity: YearEntity): Long

    // Insert only if missing (import/merge usage)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(entity: YearEntity)

    // Delete a year
    @Delete
    suspend fun deleteYear(entity: YearEntity)

    // Observe all years sorted ascending
    @Query("SELECT * FROM years ORDER BY value ASC")
    fun getAllYears(): Flow<List<YearEntity>>
}
