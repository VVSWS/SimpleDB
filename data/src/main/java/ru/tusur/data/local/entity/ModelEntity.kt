package ru.tusur.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "models",
    primaryKeys = ["name", "brandName", "yearValue"]
)
data class ModelEntity(
    val name: String,
    val brandName: String,
    val yearValue: Int
)
