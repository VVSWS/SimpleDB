package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Year
import ru.tusur.domain.repository.ReferenceDataRepository

class DeleteYearUseCase(
    private val repo: ReferenceDataRepository
) {
    suspend operator fun invoke(year: Year) = repo.deleteYear(year)
}
