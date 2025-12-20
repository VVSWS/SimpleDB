package ru.tusur.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.YearEntity

@Dao
interface YearDao {
    @Query("SELECT * FROM years ORDER BY value DESC")
    fun getAllYears(): Flow<List<YearEntity>>

    @Query("SELECT * FROM years WHERE value = :value")
    suspend fun getYearByValue(value: Int): YearEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertYear(year: YearEntity): Long
}