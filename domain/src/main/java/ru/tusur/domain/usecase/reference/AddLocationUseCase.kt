package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Location
import ru.tusur.domain.repository.ReferenceDataRepository

// ---------------------------------------------------------
// UseCase для добавления нового местоположения в справочник
// ---------------------------------------------------------
// Инкапсулирует бизнес-логику добавления локации
// Вызывает соответствующий метод репозитория
// Местоположения используются для категоризации места возникновения неисправности
class AddLocationUseCase(
    private val repository: ReferenceDataRepository   // Репозиторий для работы со справочниками
) {
    // ---------------------------------------------------------
    // Оператор invoke - выполнение добавления местоположения
    // ---------------------------------------------------------
    // location: доменная модель локации для добавления
    // Возвращает: Result<Unit> - Success при успехе, Failure с описанием ошибки
    suspend operator fun invoke(location: Location) = repository.addLocation(location)
}