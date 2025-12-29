package ru.tusur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Nullable because FaultEntry.year is nullable
    val year: Int? = null,

    // Nullable because FaultEntry.brand is nullable
    val brand: String? = null,

    // Composite model fields (nullable)
    val modelName: String? = null,
    val modelBrand: String? = null,
    val modelYear: Int? = null,

    // Nullable because FaultEntry.location is nullable
    val location: String? = null,

    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0
)
