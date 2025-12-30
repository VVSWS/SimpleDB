package ru.tusur.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class EntryWithRelations(
    @Embedded val entry: EntryEntity,

    @Relation(
        parentColumn = "year",
        entityColumn = "value"
    )
    val year: YearEntity?,

    @Relation(
        parentColumn = "brand",
        entityColumn = "name"
    )
    val brand: BrandEntity?,

    @Relation(
        parentColumn = "location",
        entityColumn = "name"
    )
    val location: LocationEntity?
)
