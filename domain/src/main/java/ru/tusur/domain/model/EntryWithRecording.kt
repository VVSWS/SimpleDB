package ru.tusur.domain.model

data class EntryWithRecording(
    val id: Long,
    val title: String,
    val year: Year,
    val model: Model,
    val location: Location,
    val date: Long,
    val audioPath: String?,
    val description: String?
)
