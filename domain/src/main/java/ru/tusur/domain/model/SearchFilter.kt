package ru.tusur.domain.model

data class SearchFilter(
    val year: String? = null,
    val model: String? = null,
    val location: String? = null
)
