package ru.tusur.domain.usecase.database

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.tusur.core.util.FileHelper
import java.io.File

data class CurrentDbInfo(
    val isActive: Boolean,
    val filename: String?,
    val entryCount: Int
)

class GetCurrentDatabaseInfoUseCase(
    private val context: Context
) {

    operator fun invoke(): Flow<CurrentDbInfo> = flow {
        val dbFile: File = FileHelper.getActiveDatabaseFile(context)

        if (!dbFile.exists()) {
            emit(CurrentDbInfo(false, null, 0))
            return@flow
        }

        // We don't open Room here — too heavy.
        // Just report file info.
        emit(
            CurrentDbInfo(
                isActive = true,
                filename = dbFile.name,
                entryCount = 0 // optional: you can add a DAO call later
            )
        )
    }
}
