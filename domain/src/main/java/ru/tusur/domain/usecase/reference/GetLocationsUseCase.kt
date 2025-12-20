package ru.tusur.domain.usecase.reference

import kotlinx.coroutines.flow.Flow
import ru.tusur.stop.domain.model.Location
import ru.tusur.stop.domain.repository.ReferenceDataRepository

class GetLocationsUseCase(
    private val repository: ReferenceDataRepository
) {
    operator fun invoke(): Flow<List<Location>> = repository.getLocations()
}