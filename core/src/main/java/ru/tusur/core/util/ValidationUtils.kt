package ru.tusur.core.util

object ValidationUtils {

    fun validateYear(year: String): Result<Int> {
        return try {
            val value = year.trim().toInt()
            if (value in 1900..2025) {
                Result.success(value)
            } else {
                Result.failure(IllegalArgumentException("Year must be between 1900 and 2025"))
            }
        } catch (e: NumberFormatException) {
            Result.failure(IllegalArgumentException("Invalid year format"))
        }
    }

    fun maxLengthTrim(input: String, maxLength: Int): String {
        return input.trim().take(maxLength)
    }

    fun nonEmpty(input: String): Boolean {
        return input.trim().isNotEmpty()
    }
}