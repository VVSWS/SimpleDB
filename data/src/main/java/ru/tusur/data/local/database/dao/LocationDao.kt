package ru.tusur.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.LocationEntity

@Dao
interface LocationDao {

    // Insert or update a location (UI usage)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(entity: LocationEntity): Long

    // Insert only if missing (import/merge usage)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(entity: LocationEntity)

    // Delete a location
    @Delete
    suspend fun deleteLocation(entity: LocationEntity)

    // Observe all locations alphabetically
    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<LocationEntity>>
}
