package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Location
import ru.tusur.domain.repository.ReferenceDataRepository

// ---------------------------------------------------------
// UseCase для удаления местоположения из справочника
// ---------------------------------------------------------
// Инкапсулирует бизнес-логику удаления локации
// Вызывает соответствующий метод репозитория
// ВНИМАНИЕ: удаление местоположения может повлиять на существующие записи,
// если они ссылаются на эту локацию (зависит от настроек внешних ключей в БД)
class DeleteLocationUseCase(
    private val repository: ReferenceDataRepository   // Репозиторий для работы со справочниками
) {
    // ---------------------------------------------------------
    // Оператор invoke - выполнение удаления местоположения
    // ---------------------------------------------------------
    // location: доменная модель локации для удаления
    suspend operator fun invoke(location: Location) = repository.deleteLocation(location)
}