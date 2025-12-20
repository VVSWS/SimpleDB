package ru.tusur.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.tusur.data.local.entity.ModelEntity

@Dao
interface ModelDao {
    @Query("SELECT * FROM models ORDER BY name COLLATE NOCASE")
    fun getAllModels(): Flow<List<ModelEntity>>

    @Query("SELECT * FROM models WHERE name = :name COLLATE NOCASE")
    suspend fun getModelByName(name: String): ModelEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertModel(model: ModelEntity): Long
}