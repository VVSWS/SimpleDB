package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Model
import ru.tusur.domain.repository.ReferenceDataRepository

class DeleteModelUseCase(
    private val repo: ReferenceDataRepository
) {
    suspend operator fun invoke(model: Model) = repo.deleteModel(model)
}
