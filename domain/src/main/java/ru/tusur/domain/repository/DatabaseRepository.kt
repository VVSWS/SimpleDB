package ru.tusur.domain.repository

import java.io.File

interface DatabaseRepository {
    suspend fun mergeDatabase(externalFile: File): Int
}
