package ru.tusur.domain.usecase.entry

import ru.tusur.stop.domain.model.FaultEntry
import ru.tusur.stop.domain.repository.FaultRepository

class GetEntryByIdUseCase(
    private val repository: FaultRepository
) {
    suspend operator fun invoke(id: Long): Result<FaultEntry?> =
        runCatching { repository.getEntryById(id) }
}