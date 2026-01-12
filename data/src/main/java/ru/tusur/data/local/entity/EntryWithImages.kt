package ru.tusur.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room relation:
 * EntryEntity + all associated EntryImageEntity rows.
 *
 * Used for detail screens, export, import, and recording view.
 */
data class EntryWithImages(
    @Embedded val entry: EntryEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "entryId"
    )
    val images: List<EntryImageEntity>
)
