package ru.tusur.domain.usecase.reference

import ru.tusur.domain.repository.ReferenceDataRepository

class GetYearsUseCase(
    private val repository: ReferenceDataRepository
) {
    operator fun invoke() = repository.getYears()
}
