package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.Year
import ru.tusur.domain.repository.ReferenceDataRepository


class GetModelsForBrandAndYearUseCase(
    private val repository: ReferenceDataRepository
) {
    operator fun invoke(brand: Brand, year: Year) =
        repository.getModelsForBrandAndYear(brand, year)
}
