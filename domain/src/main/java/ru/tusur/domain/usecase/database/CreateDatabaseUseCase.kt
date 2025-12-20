package ru.tusur.domain.usecase.database

import java.io.File

class CreateDatabaseUseCase(
    private val databasePathProvider: (isExternal: Boolean) -> File
) {
    suspend operator fun invoke(
        filename: String = "faults_${System.currentTimeMillis()}.db",
        isExternal: Boolean = false
    ): Result<File> = runCatching {
        val dbFile = File(
            databasePathProvider(isExternal).parentFile,
            filename
        )
        dbFile.parentFile?.mkdirs()
        dbFile.createNewFile()
        dbFile
    }
}