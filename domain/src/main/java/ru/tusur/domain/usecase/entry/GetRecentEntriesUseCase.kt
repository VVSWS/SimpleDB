package ru.tusur.domain.usecase.entry

import ru.tusur.domain.repository.FaultRepository

class GetRecentEntriesUseCase(
    private val repository: FaultRepository
) {
    suspend operator fun invoke(limit: Int = 20) = repository.getRecentEntries(limit)
}

