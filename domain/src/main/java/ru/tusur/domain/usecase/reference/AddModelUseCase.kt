package ru.tusur.domain.usecase.reference

import ru.tusur.stop.domain.model.Model
import ru.tusur.stop.domain.repository.ReferenceDataRepository

class AddModelUseCase(
    private val repository: ReferenceDataRepository
) {
    suspend operator fun invoke(model: Model): Result<Unit> =
        repository.addModel(model)
}