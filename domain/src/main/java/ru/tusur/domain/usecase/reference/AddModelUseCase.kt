package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Model
import ru.tusur.domain.repository.ReferenceDataRepository

// ---------------------------------------------------------
// UseCase для добавления новой модели автомобиля в справочник
// ---------------------------------------------------------
// Инкапсулирует бизнес-логику добавления модели
// Вызывает соответствующий метод репозитория
// Модель привязана к конкретной марке и году выпуска
class AddModelUseCase(
    private val repository: ReferenceDataRepository   // Репозиторий для работы со справочниками
) {
    // ---------------------------------------------------------
    // Оператор invoke - выполнение добавления модели
    // ---------------------------------------------------------
    // model: доменная модель модели для добавления (содержит name, brand, year)
    // Возвращает: Result<Unit> - Success при успехе, Failure с описанием ошибки
    suspend operator fun invoke(model: Model) = repository.addModel(model)
}