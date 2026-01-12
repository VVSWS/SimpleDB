package ru.tusur.domain.usecase.entry

import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

class DeleteEntryUseCase(
    private val repository: FaultRepository
) {
    suspend operator fun invoke(entry: FaultEntry) = repository.deleteEntry(entry)
}

