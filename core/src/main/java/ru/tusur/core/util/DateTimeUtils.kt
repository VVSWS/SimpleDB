package ru.tusur.core.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun formatTimestamp(timestamp: Long): String {
        return LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.UTC)
            .format(formatter)
    }

    fun currentTimestamp(): Long {
        return LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC)
    }
}