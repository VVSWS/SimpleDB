package ru.tusur.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.ModelEntity

@Dao
interface ModelDao {

    // Used by UI when user adds or edits a model
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(entity: ModelEntity): Long

    // Used by merge/import to avoid overwriting user changes
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(entity: ModelEntity)

    // Used by UI to delete a model
    @Delete
    suspend fun deleteModel(entity: ModelEntity)

    // Used by dropdowns (filtered by brand + year)
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

    // Optional: used for debugging or admin screens
    @Query("SELECT * FROM models ORDER BY name ASC")
    fun getAllModels(): Flow<List<ModelEntity>>
}
