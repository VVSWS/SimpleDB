package ru.tusur.domain.usecase.entry

import kotlinx.coroutines.flow.Flow
import ru.tusur.stop.domain.model.FaultEntry
import ru.tusur.stop.domain.repository.FaultRepository

class GetEntriesUseCase(
    private val repository: FaultRepository
) {
    operator fun invoke(): Flow<List<FaultEntry>> = repository.getAllEntries()
}