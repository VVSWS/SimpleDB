package ru.tusur.domain.usecase.reference

import kotlinx.coroutines.flow.Flow
import ru.tusur.stop.domain.model.Year
import ru.tusur.stop.domain.repository.ReferenceDataRepository

class GetYearsUseCase(
    private val repository: ReferenceDataRepository
) {
    operator fun invoke(): Flow<List<Year>> = repository.getYears()
}