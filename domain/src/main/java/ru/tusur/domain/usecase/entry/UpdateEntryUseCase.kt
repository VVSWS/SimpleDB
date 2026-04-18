package ru.tusur.domain.usecase.entry

import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

// ---------------------------------------------------------
// UseCase для обновления существующей записи о неисправности
// ---------------------------------------------------------
// Инкапсулирует бизнес-логику обновления записи
// Вызывает соответствующий метод репозитория
// Обновляет все поля записи, включая связанные изображения
class UpdateEntryUseCase(
    private val repository: FaultRepository   // Репозиторий для работы с записями
) {
    // ---------------------------------------------------------
    // Оператор invoke - выполнение обновления записи
    // ---------------------------------------------------------
    // entry: доменная модель записи с обновлёнными данными (должен содержать корректный id)
    suspend operator fun invoke(entry: FaultEntry) {
        repository.updateEntry(entry)
    }
}