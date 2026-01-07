package ru.tusur.domain.usecase.entry

import ru.tusur.domain.repository.FaultRepository

class DeleteImageUseCase(
    private val repository: FaultRepository
) {
    suspend operator fun invoke(path: String) {
        repository.deleteImage(path)
    }
}
