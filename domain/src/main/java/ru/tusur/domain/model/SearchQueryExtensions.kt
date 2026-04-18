package ru.tusur.domain.model

// ---------------------------------------------------------
// Функция-расширение для преобразования SearchQuery в SearchFilter
// ---------------------------------------------------------
// Преобразует запрос поиска из слоя данных обратно в модель фильтра для UI
// Выполняет обратное преобразование по сравнению с SearchFilterExtensions.toQuery()
// Позволяет конвертировать данные в обоих направлениях между слоями
fun SearchQuery.toFilter(): SearchFilter {
    return SearchFilter(
        year = this.year,        // Год выпуска
        brand = this.brand,      // Марка автомобиля
        model = this.model,      // Модель автомобиля
        location = this.location // Местоположение
    )
}