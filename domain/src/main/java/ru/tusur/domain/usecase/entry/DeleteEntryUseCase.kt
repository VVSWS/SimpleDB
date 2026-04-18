package ru.tusur.domain.usecase.entry

import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

// ---------------------------------------------------------
// UseCase для удаления записи о неисправности
// ---------------------------------------------------------
// Инкапсулирует бизнес-логику удаления записи
// Вызывает соответствующий метод репозитория
// Также удаляет все связанные с записью изображения (как связи в БД, так и физические файлы)
class DeleteEntryUseCase(
    private val repository: FaultRepository   // Репозиторий для работы с записями
) {
    // ---------------------------------------------------------
    // Оператор invoke - выполнение удаления записи
    // ---------------------------------------------------------
    // entry: доменная модель записи для удаления (используется id)
    suspend operator fun invoke(entry: FaultEntry) {
        repository.deleteEntry(entry)
    }
}