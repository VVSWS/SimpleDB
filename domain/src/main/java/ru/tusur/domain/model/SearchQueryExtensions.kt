package ru.tusur.domain.model

fun SearchQuery.toFilter(): SearchFilter {
    return SearchFilter(
        year = this.year,
        brand = this.brand,
        model = this.model,
        location = this.location
    )
}
