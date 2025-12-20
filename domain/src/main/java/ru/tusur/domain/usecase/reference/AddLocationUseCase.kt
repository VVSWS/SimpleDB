package ru.tusur.domain.usecase.reference

import ru.tusur.stop.domain.model.Location
import ru.tusur.stop.domain.repository.ReferenceDataRepository

class AddLocationUseCase(
    private val repository: ReferenceDataRepository
) {
    suspend operator fun invoke(location: Location): Result<Unit> =
        repository.addLocation(location)
}