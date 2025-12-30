package ru.tusur.domain.usecase.database

import ru.tusur.domain.repository.DatabaseMergeRepository
import java.io.File

class MergeDatabaseUseCase(
    private val repository: DatabaseMergeRepository
) {
    suspend operator fun invoke(externalDb: File): Int {
        return repository.mergeDatabase(externalDb)
    }
}
