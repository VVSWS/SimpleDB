package ru.tusur.domain.model

data class FaultEntry(
    val id: Long = 0L,
    val year: Year = Year(2025),
    val model: Model = Model(""),
    val location: Location = Location(""),
    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0L,
    val imageUris: List<String> = emptyList()
)
