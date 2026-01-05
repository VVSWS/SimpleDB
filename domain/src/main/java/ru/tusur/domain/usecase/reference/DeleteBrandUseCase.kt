package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Brand
import ru.tusur.domain.repository.ReferenceDataRepository

class DeleteBrandUseCase(
    private val repo: ReferenceDataRepository
) {
    suspend operator fun invoke(brand: Brand) = repo.deleteBrand(brand)
}
