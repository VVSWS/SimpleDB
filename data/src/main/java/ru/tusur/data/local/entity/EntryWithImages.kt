package ru.tusur.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class EntryWithImages(
    @Embedded val entry: EntryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "entryId"
    )
    val images: List<EntryImageEntity>
)