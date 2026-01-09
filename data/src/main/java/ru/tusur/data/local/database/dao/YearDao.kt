package ru.tusur.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.YearEntity

@Dao
interface YearDao {

    // UI adds or edits a year
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYear(entity: YearEntity): Long

    // Merge/import adds missing years without overwriting user changes
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(entity: YearEntity)

    // UI deletes a year
    @Delete
    suspend fun deleteYear(entity: YearEntity)

    // Dropdown list
    @Query("SELECT * FROM years ORDER BY value ASC")
    fun getAllYears(): Flow<List<YearEntity>>
}
