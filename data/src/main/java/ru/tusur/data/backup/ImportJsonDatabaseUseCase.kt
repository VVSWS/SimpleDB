package ru.tusur.data.backup

import android.content.Context
import kotlinx.serialization.json.Json
import ru.tusur.data.local.DatabaseProvider
import ru.tusur.domain.export.ExportDatabase
import ru.tusur.domain.model.*
import ru.tusur.domain.repository.FaultRepository
import ru.tusur.domain.repository.ReferenceDataRepository
import java.io.File
import java.io.InputStream

class ImportJsonDatabaseUseCase(
    private val context: Context,
    private val provider: DatabaseProvider,
    private val faultRepository: FaultRepository,
    private val referenceRepository: ReferenceDataRepository
) {

    suspend operator fun invoke(inputStream: InputStream): Result<Unit> {
        return try {

            // ---------------------------------------------------------
            // 1. Read JSON
            // ---------------------------------------------------------
            val json = inputStream.bufferedReader().use { it.readText() }
            val export = Json.decodeFromString<ExportDatabase>(json)

            // Images expected in: /files/backup/images/<oldEntryId>/
            val backupDir = File(context.filesDir, "backup/images")

            // ---------------------------------------------------------
            // 2. Insert entries + dictionary values
            // ---------------------------------------------------------
            export.entries.forEach { dto ->

                val brandName = dto.brand ?: error("Missing brand")
                val locationName = dto.location ?: error("Missing location")
                val modelName = dto.modelName ?: error("Missing modelName")
                val yearValue = dto.year ?: error("Missing year")

                val brand = Brand(brandName)
                val location = Location(locationName)
                val year = Year(yearValue)
                val model = Model(
                    name = modelName,
                    brand = brand,
                    year = year
                )

                // Insert dictionary values
                referenceRepository.addBrand(brand)
                referenceRepository.addLocation(location)
                referenceRepository.addYear(year)
                referenceRepository.addModel(model)

                // ---------------------------------------------------------
                // Create FaultEntry (domain model)
                // ---------------------------------------------------------
                val entry = FaultEntry(
                    id = 0, // new entry → auto-generate
                    timestamp = dto.timestamp,
                    year = year,
                    brand = brand,
                    model = model,
                    location = location,
                    title = dto.title,
                    description = dto.description,
                    imageUris = emptyList()
                )

                // Insert into DB
                val newEntryId = faultRepository.createEntry(entry)

                // ---------------------------------------------------------
                // 3. Copy images from backup folder into app storage
                // ---------------------------------------------------------
                val entryBackupDir = File(backupDir, dto.id.toString())
                if (entryBackupDir.exists()) {

                    val dstDir = File(context.filesDir, "images/$newEntryId")
                    dstDir.mkdirs()

                    entryBackupDir.listFiles()?.forEach { src ->
                        val dst = File(dstDir, src.name)
                        src.copyTo(dst, overwrite = true)

                        // Register image in DB
                        faultRepository.addImageToEntry(
                            entryId = newEntryId,
                            uri = dst.toURI().toString()
                        )
                    }
                }
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
