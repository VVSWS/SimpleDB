package ru.tusur.domain.model

data class EntryWithRecording(
    val id: Long,
    val title: String,
    val year: Year,
    val brand: Brand,
    val model: Model,
    val location: Location,
    val timestamp: Long,
    val description: String?,
    val imageUris: List<String> = emptyList()
)

