package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Year
import ru.tusur.domain.repository.ReferenceDataRepository

// ---------------------------------------------------------
// UseCase для добавления нового года выпуска в справочник
// ---------------------------------------------------------
// Инкапсулирует бизнес-логику добавления года
// Вызывает соответствующий метод репозитория
// Годы используются для фильтрации записей и выбора модели автомобиля
class AddYearUseCase(
    private val repository: ReferenceDataRepository   // Репозиторий для работы со справочниками
) {
    // ---------------------------------------------------------
    // Оператор invoke - выполнение добавления года
    // ---------------------------------------------------------
    // year: доменная модель года для добавления
    // Возвращает: Result<Unit> - Success при успехе, Failure с описанием ошибки
    suspend operator fun invoke(year: Year) = repository.addYear(year)
}