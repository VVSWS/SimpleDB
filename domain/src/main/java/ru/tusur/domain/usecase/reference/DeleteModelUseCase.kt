package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Model
import ru.tusur.domain.repository.ReferenceDataRepository

class DeleteModelUseCase(
    private val repository: ReferenceDataRepository
) {
    suspend operator fun invoke(model: Model) = repository.deleteModel(model)
}
