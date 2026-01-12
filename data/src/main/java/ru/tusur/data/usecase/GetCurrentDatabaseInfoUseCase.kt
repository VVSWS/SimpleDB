package ru.tusur.data.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.tusur.data.local.DatabaseProvider
import java.io.File

data class CurrentDbInfo(
    val isActive: Boolean,
    val filename: String?,
    val entryCount: Int
)

class GetCurrentDatabaseInfoUseCase(
    private val provider: DatabaseProvider
) {

    operator fun invoke(): Flow<CurrentDbInfo> = flow {
        val dbFile: File = provider.getActiveDatabaseFile()

        if (!dbFile.exists()) {
            emit(CurrentDbInfo(false, null, 0))
            return@flow
        }

        val count = provider.getEntryCountSafe()

        emit(
            CurrentDbInfo(
                isActive = true,
                filename = dbFile.name,
                entryCount = count
            )
        )
    }
}
