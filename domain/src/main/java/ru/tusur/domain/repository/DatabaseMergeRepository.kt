package ru.tusur.domain.repository

import java.io.File

interface DatabaseMergeRepository {
    suspend fun mergeDatabase(externalDb: File): Int
}
