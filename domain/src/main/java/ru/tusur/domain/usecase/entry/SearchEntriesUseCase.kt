package ru.tusur.domain.usecase.entry

import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

class SearchEntriesUseCase(
    private val repository: FaultRepository
) {
    suspend operator fun invoke(
        year: String?,
        model: String?,
        location: String?
    ): Result<List<FaultEntry>> {
        return runCatching { repository.searchEntries(year, model, location) }
    }
}
