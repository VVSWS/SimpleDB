package ru.tusur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a fault entry.
 *
 * This schema remains stable because:
 * - Dictionary values (year, brand, model, location) are stored as simple fields
 *   and resolved through relations in EntryWithRelations / EntryWithImages.
 * - Backup/export/import rely on these fields.
 * - Dynamic DB architecture does not change the schema, only how DB instances are selected.
 */
@Entity(tableName = "entries")
data class EntryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Dictionary references
    val year: Int? = null,
    val brand: String? = null,

    val modelName: String? = null,
    val modelBrand: String? = null,
    val modelYear: Int? = null,

    val location: String? = null,

    // Entry content
    val title: String = "",
    val description: String = "",

    // Metadata
    val timestamp: Long = 0
)
