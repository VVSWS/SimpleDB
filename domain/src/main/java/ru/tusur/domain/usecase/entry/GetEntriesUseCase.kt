package ru.tusur.domain.usecase.entry

import kotlinx.coroutines.flow.Flow
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.repository.FaultRepository

class GetEntriesUseCase(
    private val repository: FaultRepository
) {
    operator fun invoke(): Flow<List<FaultEntry>> = repository.getAllEntries()
}