package ru.tusur.data.local.database.dao

import androidx.room.*
import ru.tusur.data.local.entity.EntryImageEntity

@Dao
interface EntryImageDao {

    // READ
    @Query("SELECT * FROM entry_images WHERE entryId = :entryId")
    suspend fun getImagesForEntry(entryId: Long): List<EntryImageEntity>

    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: EntryImageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<EntryImageEntity>)

    // DELETE
    @Query("DELETE FROM entry_images WHERE entryId = :entryId")
    suspend fun deleteImagesForEntry(entryId: Long)

    @Query("DELETE FROM entry_images WHERE entryId = :entryId AND uri = :uri")
    suspend fun deleteImage(entryId: Long, uri: String)

    @Query("DELETE FROM entry_images WHERE uri = :uri")
    suspend fun deleteImageByUri(uri: String)

    @Query("DELETE FROM entry_images")
    suspend fun deleteAllImages()

}
