package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Brand
import ru.tusur.domain.repository.ReferenceDataRepository

// ---------------------------------------------------------
// UseCase для добавления новой марки автомобиля в справочник
// ---------------------------------------------------------
// Инкапсулирует бизнес-логику добавления марки
// Вызывает соответствующий метод репозитория
// Возвращает Result<Unit> - успех или ошибку (например, если марка уже существует)
class AddBrandUseCase(
    private val repository: ReferenceDataRepository   // Репозиторий для работы со справочниками
) {
    // ---------------------------------------------------------
    // Оператор invoke - выполнение добавления марки
    // ---------------------------------------------------------
    // brand: доменная модель марки для добавления
    // Возвращает: Result<Unit> - Success при успехе, Failure с описанием ошибки
    suspend operator fun invoke(brand: Brand) = repository.addBrand(brand)
}