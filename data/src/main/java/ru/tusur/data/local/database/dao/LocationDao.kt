package ru.tusur.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.LocationEntity

@Dao
interface LocationDao {

    // Used by UI when user adds a location manually
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(entity: LocationEntity): Long

    // Used by merge/import to avoid overwriting user changes
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(entity: LocationEntity)

    // Used by UI to delete a location
    @Delete
    suspend fun deleteLocation(entity: LocationEntity)

    // Used by dropdowns
    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<LocationEntity>>
}
