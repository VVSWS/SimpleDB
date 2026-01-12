package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Brand
import ru.tusur.domain.repository.ReferenceDataRepository

class DeleteBrandUseCase(
    private val repository: ReferenceDataRepository
) {
    suspend operator fun invoke(brand: Brand) = repository.deleteBrand(brand)
}

