package ru.tusur.domain.usecase.entry

import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

class SearchEntriesUseCase(
    private val repository: FaultRepository
) {
    suspend operator fun invoke(
        year: Int?,
        brand: String?,
        model: String?,
        location: String?
    ) = repository.searchEntries(year, brand, model, location)
}

