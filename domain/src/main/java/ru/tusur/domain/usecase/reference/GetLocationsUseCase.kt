package ru.tusur.domain.usecase.reference

import ru.tusur.domain.repository.ReferenceDataRepository

class GetLocationsUseCase(
    private val repository: ReferenceDataRepository
) {
    operator fun invoke() = repository.getLocations()
}
