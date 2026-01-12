package ru.tusur.domain.model

data class EntryWithImages(
    val entry: FaultEntry,
    val imageUris: List<String>
)
