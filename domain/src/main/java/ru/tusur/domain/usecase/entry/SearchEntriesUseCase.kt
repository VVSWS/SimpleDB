package ru.tusur.domain.usecase.entry

import ru.tusur.stop.domain.model.FaultEntry
import ru.tusur.stop.domain.repository.FaultRepository

class SearchEntriesUseCase(
    private val repository: FaultRepository
) {
    suspend operator fun invoke(
        year: Int? = null,
        model: String? = null,
        location: String? = null
    ): Result<List<FaultEntry>> = runCatching {
        repository.searchEntries(year, model, location)
    }
}