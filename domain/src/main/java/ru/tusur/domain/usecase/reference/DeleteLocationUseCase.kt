package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Location
import ru.tusur.domain.repository.ReferenceDataRepository

class DeleteLocationUseCase(
    private val repository: ReferenceDataRepository
) {
    suspend operator fun invoke(location: Location) = repository.deleteLocation(location)
}
