package ru.tusur.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.ModelEntity

@Dao
interface ModelDao {

    // Insert or update a model (UI usage)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(entity: ModelEntity): Long

    // Insert only if missing (import/merge usage)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(entity: ModelEntity)

    // Delete a model
    @Delete
    suspend fun deleteModel(entity: ModelEntity)

    // Observe models filtered by brand + year
    @Query("""
        SELECT * FROM models
        WHERE brandName = :brandName
          AND yearValue = :yearValue
        ORDER BY name ASC
    """)
    fun getModelsForBrandAndYear(
        brandName: String,
        yearValue: Int
    ): Flow<List<ModelEntity>>

    // Observe all models alphabetically
    @Query("SELECT * FROM models ORDER BY name ASC")
    fun getAllModels(): Flow<List<ModelEntity>>
}
