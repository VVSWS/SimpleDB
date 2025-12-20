package ru.tusur.domain.usecase.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import ru.tusur.domain.repository.FaultRepository

class GetCurrentDatabaseInfoUseCase(
    private val faultRepository: FaultRepository
) {
    data class DbInfo(
        val isActive: Boolean,
        val filename: String? = null,
        val entryCount: Int = 0
    )

    operator fun invoke(): Flow<DbInfo> = flow {
        try {
            faultRepository.getAllEntries().collect { entries ->
                emit(DbInfo(
                    isActive = true,
                    filename = "current.db",
                    entryCount = entries.size
                ))
            }
        } catch (e: Exception) {
            emit(DbInfo(isActive = false))
        }
    }
}