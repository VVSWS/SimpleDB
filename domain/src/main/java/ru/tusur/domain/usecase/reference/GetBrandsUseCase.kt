package ru.tusur.domain.usecase.reference

import kotlinx.coroutines.flow.Flow
import ru.tusur.domain.model.Brand
import ru.tusur.domain.repository.ReferenceDataRepository

class GetBrandsUseCase(
    private val repository: ReferenceDataRepository
) {
    operator fun invoke(): Flow<List<Brand>> = repository.getBrands()
}