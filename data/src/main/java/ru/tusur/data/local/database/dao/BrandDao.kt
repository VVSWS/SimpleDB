package ru.tusur.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.BrandEntity

@Dao
interface BrandDao {

    // Used by UI when user adds or edits a brand
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrand(entity: BrandEntity): Long

    // Used by merge/import to avoid overwriting user changes
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(entity: BrandEntity)

    // Used by UI to delete a brand
    @Delete
    suspend fun deleteBrand(entity: BrandEntity)

    // Used by dropdowns
    @Query("SELECT * FROM brands ORDER BY name ASC")
    fun getAllBrands(): Flow<List<BrandEntity>>
}
