package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Model
import ru.tusur.domain.repository.ReferenceDataRepository

class AddModelUseCase(
    private val repository: ReferenceDataRepository
) {
    suspend operator fun invoke(model: Model): Result<Unit> {
        return repository.addModel(model)
    }
}
