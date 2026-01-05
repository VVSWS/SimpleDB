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

    @Query("SELECT * FROM brands ORDER BY name ASC")
    fun getAllBrands(): Flow<List<BrandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrand(entity: BrandEntity): Long

    @Delete
    suspend fun deleteBrand(entity: BrandEntity)

}
