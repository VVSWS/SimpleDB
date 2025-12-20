package ru.tusur.domain.usecase.database

import kotlinx.coroutines.flow.Flow
import ru.tusur.stop.domain.repository.FaultRepository

class GetCurrentDatabaseInfoUseCase(
    private val faultRepository: FaultRepository
) {
    data class DbInfo(
        val isActive: Boolean,
        val filename: String? = null,
        val entryCount: Int = 0
    )

    operator fun invoke(): Flow<DbInfo> {
        return faultRepository.getAllEntries().map { entries ->
            DbInfo(
                isActive = true,
                filename = "current.db",
                entryCount = entries.size
            )
        }.catch { emit(DbInfo(isActive = false)) }
    }

    private fun <T> Flow<T>.catch(block: suspend (Throwable) -> Unit): Flow<T> =
        this
}