package ru.tusur.domain.usecase.entry

import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

class GetEntryByIdUseCase(
    private val repository: FaultRepository
) {
    suspend operator fun invoke(id: Long) = repository.getEntryById(id)
}

