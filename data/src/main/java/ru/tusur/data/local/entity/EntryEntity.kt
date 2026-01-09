package ru.tusur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val year: Int? = null,
    val brand: String? = null,

    val modelName: String? = null,
    val modelBrand: String? = null,
    val modelYear: Int? = null,

    val location: String? = null,

    val title: String = "",
    val description: String = "",

    // ⭐ Add this field
    val notes: String? = null,

    val timestamp: Long = 0
)
