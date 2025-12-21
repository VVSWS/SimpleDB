package ru.tusur.domain.repository

import java.io.File

interface DatabaseValidator {
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    suspend fun validateDatabase(dbFile: File): ValidationResult
}