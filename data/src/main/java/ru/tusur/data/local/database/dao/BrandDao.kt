package ru.tusur.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.BrandEntity

@Dao
interface BrandDao {

    @Query("SELECT * FROM brands ORDER BY name COLLATE NOCASE")
    fun getAllBrands(): Flow<List<BrandEntity>>

    @Query("SELECT * FROM brands WHERE name = :name COLLATE NOCASE")
    suspend fun getBrandByName(name: String): BrandEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBrand(brand: BrandEntity): Long
}
