package ru.tusur.domain.usecase.entry

import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

// ---------------------------------------------------------
// UseCase для создания новой записи о неисправности
// ---------------------------------------------------------
// Инкапсулирует бизнес-логику создания записи
// Вызывает соответствующий метод репозитория
// Возвращает сгенерированный ID новой записи
class CreateEntryUseCase(
    private val repository: FaultRepository   // Репозиторий для работы с записями
) {
    // ---------------------------------------------------------
    // Оператор invoke - выполнение создания записи
    // ---------------------------------------------------------
    // entry: доменная модель записи для сохранения (id обычно = 0)
    // Возвращает: Long - сгенерированный базой данных ID записи
    suspend operator fun invoke(entry: FaultEntry): Long {
        return repository.createEntry(entry)
    }
}