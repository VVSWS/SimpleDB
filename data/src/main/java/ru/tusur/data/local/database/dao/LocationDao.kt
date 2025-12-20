package ru.tusur.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.LocationEntity

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY name COLLATE NOCASE")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE name = :name COLLATE NOCASE")
    suspend fun getLocationByName(name: String): LocationEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocation(location: LocationEntity): Long
}