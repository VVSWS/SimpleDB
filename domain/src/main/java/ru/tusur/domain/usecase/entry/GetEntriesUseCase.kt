package ru.tusur.domain.usecase.entry

import ru.tusur.domain.repository.FaultRepository

class GetEntriesUseCase(
    private val repository: FaultRepository
) {
    operator fun invoke() = repository.getAllEntries()
}
