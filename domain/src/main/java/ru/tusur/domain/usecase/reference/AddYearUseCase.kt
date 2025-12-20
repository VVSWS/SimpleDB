package ru.tusur.domain.usecase.reference

import ru.tusur.stop.domain.model.Year
import ru.tusur.stop.domain.repository.ReferenceDataRepository

class AddYearUseCase(
    private val repository: ReferenceDataRepository
) {
    suspend operator fun invoke(year: Year): Result<Unit> =
        repository.addYear(year)
}