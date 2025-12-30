package ru.tusur.domain.model

data class SearchQuery(
    val year: Int?,
    val brand: String?,
    val model: String?,
    val location: String?
) {
    val isEmpty: Boolean
        get() = year == null && brand == null && model == null && location == null
}
