package ru.tusur.data.local.entity

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

// ---------------------------------------------------------
// Конвертеры типов для Room
// ---------------------------------------------------------
// Преобразуют сложные типы Kotlin в типы, понятные SQLite
// SQLite поддерживает только: NULL, INTEGER, REAL, TEXT, BLOB
object Converters {

    // ---------------------------------------------------------
    // Конвертер: Long → LocalDateTime
    // ---------------------------------------------------------
    // Преобразует временную метку (Unix timestamp в секундах) в объект LocalDateTime
    // Используется при чтении из БД
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        // Если значение null, возвращаем null
        // Иначе создаём LocalDateTime из секунд с начала эпохи (UTC)
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    // ---------------------------------------------------------
    // Конвертер: LocalDateTime → Long
    // ---------------------------------------------------------
    // Преобразует объект LocalDateTime в Unix timestamp (секунды с 1970-01-01 UTC)
    // Используется при записи в БД
    @TypeConverter
    fun toTimestamp(date: LocalDateTime?): Long? {
        // Если дата null, возвращаем null
        // Иначе преобразуем в секунды с начала эпохи
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    // ---------------------------------------------------------
    // Конвертер: List<String> → String
    // ---------------------------------------------------------
    // Преобразует список строк в одну строку с разделителем "|"
    // Используется для хранения списков в TEXT-поле SQLite
    // Пример: ["a", "b", "c"] → "a|b|c"
    @TypeConverter
    fun fromList(list: List<String>): String =
        list.joinToString(separator = "|")

    // ---------------------------------------------------------
    // Конвертер: String → List<String>
    // ---------------------------------------------------------
    // Преобразует строку с разделителем "|" обратно в список строк
    // Используется при чтении из БД
    // Пример: "a|b|c" → ["a", "b", "c"]
    // Пустая строка возвращает пустой список
    @TypeConverter
    fun toList(data: String): List<String> =
        if (data.isEmpty()) emptyList() else data.split("|")
}