package ru.tusur.domain.usecase.reference

import kotlinx.coroutines.flow.Flow
import ru.tusur.domain.model.Model
import ru.tusur.domain.repository.ReferenceDataRepository

class GetModelsUseCase(
    private val repository: ReferenceDataRepository
) {
    operator fun invoke() = repository.getModels()
}
