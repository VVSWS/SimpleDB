package ru.tusur.core.model

sealed class Failure {
    object Network : Failure()
    object Data : Failure()
    object Validation : Failure()
    data class Unknown(val error: Throwable) : Failure()

    companion object {
        fun from(error: Throwable): Failure = when (error) {
            is IllegalArgumentException -> Validation
            else -> Unknown(error)
        }
    }
}