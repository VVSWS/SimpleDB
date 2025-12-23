package ru.tusur.domain.usecase.entry

import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

class GetRecentEntriesUseCase(
    private val repository: FaultRepository
) {
    suspend operator fun invoke(): Result<List<FaultEntry>> {
        return runCatching { repository.getRecentEntries() }
    }
}
