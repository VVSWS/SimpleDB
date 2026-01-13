package ru.tusur.domain.repository

interface DatabaseMaintenanceRepository {
    suspend fun vacuum()
}
