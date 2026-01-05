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

    @Query("SELECT * FROM years ORDER BY value DESC")
    fun getAllYears(): Flow<List<YearEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYear(entity: YearEntity): Long

    @Delete
    suspend fun deleteYear(entity: YearEntity)

}
