package ru.tusur.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.tusur.data.local.entity.EntryImageEntity

@Dao
interface EntryImageDao {

    @Query("SELECT * FROM entry_images WHERE entryId = :entryId")
    suspend fun getImagesForEntry(entryId: Long): List<EntryImageEntity>

    @Query("DELETE FROM entry_images WHERE entryId = :entryId")
    suspend fun deleteImagesForEntry(entryId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: EntryImageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<EntryImageEntity>)

    @Query("DELETE FROM entry_images WHERE uri = :uri")
    suspend fun deleteImageByUri(uri: String)
}
