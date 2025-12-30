package ru.tusur.domain.model

fun SearchFilter.toQuery(): SearchQuery {
    return SearchQuery(
        year = this.year,
        brand = this.brand,
        model = this.model,
        location = this.location
    )
}
