package ru.tusur.domain.usecase.entry

import ru.tusur.stop.domain.model.FaultEntry
import ru.tusur.stop.domain.repository.FaultRepository

class UpdateEntryUseCase(
    private val repository: FaultRepository
) {
    suspend operator fun invoke(entry: FaultEntry): Result<Unit> =
        runCatching { repository.updateEntry(entry) }
}