package ru.tusur.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.ModelEntity

@Dao
interface ModelDao {

    @Query("SELECT * FROM models")
    fun getAllModels(): Flow<List<ModelEntity>>

    @Query("""
        SELECT * FROM models
        WHERE brandName = :brandName
        AND yearValue = :yearValue
    """)
    fun getModelsForBrandAndYear(
        brandName: String,
        yearValue: Int
    ): Flow<List<ModelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: ModelEntity)
}
