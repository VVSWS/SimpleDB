package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Model
import ru.tusur.domain.repository.ReferenceDataRepository

// ---------------------------------------------------------
// UseCase для удаления модели автомобиля из справочника
// ---------------------------------------------------------
// Инкапсулирует бизнес-логику удаления модели
// Вызывает соответствующий метод репозитория
// ВНИМАНИЕ: удаление модели может повлиять на существующие записи,
// если они ссылаются на эту модель (зависит от настроек внешних ключей в БД)
class DeleteModelUseCase(
    private val repository: ReferenceDataRepository   // Репозиторий для работы со справочниками
) {
    // ---------------------------------------------------------
    // Оператор invoke - выполнение удаления модели
    // ---------------------------------------------------------
    // model: доменная модель модели для удаления (содержит name, brand, year)
    suspend operator fun invoke(model: Model) = repository.deleteModel(model)
}