package ru.tusur.data.local.entity

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

object Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun toTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun fromList(list: List<String>): String =
        list.joinToString(separator = "|")

    @TypeConverter
    fun toList(data: String): List<String> =
        if (data.isEmpty()) emptyList() else data.split("|")
}