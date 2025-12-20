package ru.tusur.domain.usecase.database

import ru.tusur.stop.data.local.DatabaseValidator
import ru.tusur.stop.core.util.FileHelper
import java.io.File

class OpenDatabaseUseCase(
    private val validator: DatabaseValidator
) {
    suspend operator fun invoke(sourceFile: File): Result<File> = runCatching {
        // 1. Копируем в private storage
        val destFile = File(sourceFile.parentFile, "current.db")
        if (!FileHelper.copyFile(sourceFile, destFile)) {
            throw IllegalStateException("Failed to copy database file")
        }

        // 2. Валидация
        return when (val result = validator.validateDatabase(destFile)) {
            is DatabaseValidator.ValidationResult.Success -> Result.success(destFile)
            is DatabaseValidator.ValidationResult.Error -> Result.failure(
                IllegalStateException(result.message)
            )
        }
    }
}