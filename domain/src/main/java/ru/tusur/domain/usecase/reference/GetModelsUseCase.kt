package ru.tusur.domain.usecase.reference

import kotlinx.coroutines.flow.Flow
import ru.tusur.stop.domain.model.Model
import ru.tusur.stop.domain.repository.ReferenceDataRepository

class GetModelsUseCase(
    private val repository: ReferenceDataRepository
) {
    operator fun invoke(): Flow<List<Model>> = repository.getModels()
}