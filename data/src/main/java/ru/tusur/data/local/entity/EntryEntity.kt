package ru.tusur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: String,
    val model: String,
    val location: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val audioPath: String? = null
)
