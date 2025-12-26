package ru.tusur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "years")
data class YearEntity(
    @PrimaryKey val value: String
)