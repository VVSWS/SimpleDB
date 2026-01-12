package ru.tusur.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.BrandEntity

@Dao
interface BrandDao {

    // Insert or update a brand (UI usage)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrand(entity: BrandEntity): Long

    // Insert only if missing (import/merge usage)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(entity: BrandEntity)

    // Delete a brand
    @Delete
    suspend fun deleteBrand(entity: BrandEntity)

    // Observe all brands alphabetically
    @Query("SELECT * FROM brands ORDER BY name ASC")
    fun getAllBrands(): Flow<List<BrandEntity>>
}
