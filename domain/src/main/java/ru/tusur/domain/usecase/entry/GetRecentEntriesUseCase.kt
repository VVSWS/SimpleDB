package ru.tusur.domain.usecase.entry

import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

class GetRecentEntriesUseCase(
    private val repository: FaultRepository
) {
    suspend operator fun invoke(limit: Int = 5): Result<List<FaultEntry>> =
        runCatching { repository.getRecentEntries(limit) }
}