package ru.tusur.domain.model

data class FaultEntry(
    val id: Long = 0L,
    val year: Year? = null,
    val brand: Brand? = null,
    val model: Model? = null,
    val location: Location? = null,
    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0L,
    val imageUris: List<String> = emptyList()
)
