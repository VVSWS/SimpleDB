package ru.tusur.domain.model

data class SearchFilter(
    val year: Int? = null,
    val brand: String? = null,
    val model: String? = null,
    val location: String? = null
)
