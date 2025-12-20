package ru.tusur.domain.model

data class FaultEntry(
    val id: Long = 0,
    val year: Year,
    val model: Model,
    val location: Location,
    val title: String,
    val description: String,
    val timestamp: Long,
    val imageUris: List<String> = emptyList()
)