package ru.tusur.data.repository

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.tusur.core.files.ImageStorage
import ru.tusur.data.local.DatabaseProvider
import ru.tusur.data.local.entity.EntryImageEntity
import ru.tusur.data.mapper.EntryMapper
import ru.tusur.domain.model.EntryWithImages
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

class DefaultFaultRepository(
    private val appContext: Context,
    private val provider: DatabaseProvider,
    private val mapper: EntryMapper
) : FaultRepository {

    private val entryDao get() = provider.getCurrentDatabase().entryDao()
    private val imageDao get() = provider.getCurrentDatabase().entryImageDao()

    // ---------------------------------------------------------
    // Entry queries
    // ---------------------------------------------------------

    override fun getAllEntries(): Flow<List<FaultEntry>> {
        return entryDao.getAllEntries().map { list ->
            list.map { rel ->
                val model = entryDao.getModelForEntry(
                    rel.entry.modelName,
                    rel.entry.modelBrand,
                    rel.entry.modelYear
                )
                mapper.fromRelations(rel, model)
            }
        }
    }

    override suspend fun getEntryById(id: Long): FaultEntry? {
        val rel = entryDao.getEntryById(id) ?: return null

        val model = entryDao.getModelForEntry(
            rel.entry.modelName,
            rel.entry.modelBrand,
            rel.entry.modelYear
        )

        return mapper.fromImages(rel, model)
    }

    override suspend fun getRecentEntries(limit: Int): List<FaultEntry> {
        return entryDao.getRecentEntries().map { entity ->
            mapper.toDomain(entity)
        }
    }

    override suspend fun searchEntries(
        year: Int?,
        brand: String?,
        model: String?,
        location: String?
    ): List<FaultEntry> {
        return entryDao.searchEntries(year, brand, model, location).map { entity ->
            mapper.toDomain(entity)
        }
    }

    // ---------------------------------------------------------
    // Entry CRUD
    // ---------------------------------------------------------

    override suspend fun createEntry(entry: FaultEntry): Long {
        val db = provider.getCurrentDatabase()

        return db.withTransaction {
            val id = entryDao.insertEntry(mapper.toEntity(entry))

            entry.imageUris.forEach { uri ->
                imageDao.insertImage(
                    EntryImageEntity(
                        entryId = id,
                        uri = uri
                    )
                )
            }

            id
        }
    }




    override suspend fun updateEntry(entry: FaultEntry) {
        entryDao.updateEntry(mapper.toEntity(entry))

        // Replace images atomically
        imageDao.deleteImagesForEntry(entry.id)
        entry.imageUris.forEach { uri ->
            imageDao.insertImage(EntryImageEntity(entryId = entry.id, uri = uri))
        }
    }

    override suspend fun deleteEntry(entry: FaultEntry) {
        val db = provider.getCurrentDatabase()

        db.withTransaction {
            imageDao.deleteImagesForEntry(entry.id)
            entryDao.deleteEntryById(entry.id)   // ← correct
        }

        entry.imageUris.forEach { uri ->
            ImageStorage.deleteImageFile(appContext, uri)
        }
    }


    override suspend fun getEntryCount(): Int {
        return entryDao.getEntryCount()
    }

    // ---------------------------------------------------------
    // Images
    // ---------------------------------------------------------

    override suspend fun addImageToEntry(entryId: Long, uri: String) {
        imageDao.insertImage(EntryImageEntity(entryId = entryId, uri = uri))
    }

    override suspend fun removeImageFromEntry(entryId: Long, uri: String) {
        imageDao.deleteImage(entryId, uri)
        ImageStorage.deleteImageFile(appContext, uri)
    }

    override suspend fun deleteImage(uri: String) {
        ImageStorage.deleteImageFile(appContext, uri)
    }

    override suspend fun getEntriesWithImages(): List<EntryWithImages> {
        return entryDao.getAllEntriesWithImages().map { rel ->
            val model = entryDao.getModelForEntry(
                rel.entry.modelName,
                rel.entry.modelBrand,
                rel.entry.modelYear
            )
            mapper.toEntryWithImages(rel, model)
        }
    }

    // ---------------------------------------------------------
    // Recording
    // ---------------------------------------------------------

    override suspend fun getEntryWithRecording(id: Long): EntryWithRecording {
        val rel = entryDao.getEntryWithRecording(id)

        val model = entryDao.getModelForEntry(
            rel.entry.modelName,
            rel.entry.modelBrand,
            rel.entry.modelYear
        )

        // Convert Room EntryWithImages → Domain EntryWithImages
        val domainEntryWithImages = mapper.toEntryWithImages(rel, model)

        // Now convert to EntryWithRecording
        return mapper.toRecording(domainEntryWithImages, model)
    }

    // ---------------------------------------------------------
    // Reset database (entries + images only)
    // ---------------------------------------------------------

    override suspend fun resetDatabase() {
        // 1. Delete all entry rows
        entryDao.deleteAllEntries()

        // 2. Delete all image rows
        imageDao.deleteAllImages()

        // 3. Delete all image files
        ImageStorage.clearAllImages(appContext.filesDir)
    }
}
