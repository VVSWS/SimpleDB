package ru.tusur.data.local

import android.database.sqlite.SQLiteDatabase
import java.io.File
import ru.tusur.domain.repository.DatabaseValidator as DomainDatabaseValidator

// ---------------------------------------------------------
// Валидатор базы данных Room
// ---------------------------------------------------------
// Проверяет целостность и корректность файла базы данных SQLite
// Реализует интерфейс DomainDatabaseValidator из слоя domain
// Используется перед переключением на новую БД для проверки её пригодности
class RoomDatabaseValidator : DomainDatabaseValidator {

    // ---------------------------------------------------------
    // Список обязательных таблиц, которые должны присутствовать в БД
    // ---------------------------------------------------------
    companion object {
        private val REQUIRED_TABLES = listOf(
            "entries",        // Таблица записей о неисправностях
            "years",          // Справочник годов
            "models",         // Справочник моделей
            "locations",      // Справочник местоположений
            "entry_images"    // Связующая таблица для изображений
        )
    }

    // ---------------------------------------------------------
    // Валидация файла базы данных
    // ---------------------------------------------------------
    // Проверяет: существование файла, версию схемы, наличие всех таблиц
    // Возвращает ValidationResult (Success или Error с описанием)
    override suspend fun validateDatabase(dbFile: File): DomainDatabaseValidator.ValidationResult {
        // ---------------------------------------------------------
        // 1. Проверка существования файла
        // ---------------------------------------------------------
        if (!dbFile.exists()) {
            return DomainDatabaseValidator.ValidationResult.Error("File does not exist")
        }

        // ---------------------------------------------------------
        // 2. Открытие базы данных в режиме "только чтение"
        // ---------------------------------------------------------
        return try {
            SQLiteDatabase.openDatabase(
                dbFile.absolutePath,           // Полный путь к файлу БД
                null,                          // CursorFactory (не используется)
                SQLiteDatabase.OPEN_READONLY   // Режим только для чтения (без изменений)
            ).use { db ->                      // use автоматически закроет БД после выполнения

                // ---------------------------------------------------------
                // 2.1 Проверка версии схемы базы данных
                // ---------------------------------------------------------
                val userVersion = db.version
                if (userVersion != 1) {  // Ожидаемая версия = 1
                    return DomainDatabaseValidator.ValidationResult.Error(
                        "Incompatible schema version: $userVersion"
                    )
                }

                // ---------------------------------------------------------
                // 2.2 Получение списка всех таблиц в БД
                // ---------------------------------------------------------
                // Запрос к системной таблице sqlite_master
                val cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table'",
                    null
                )

                val tables = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    tables.add(cursor.getString(0))  // Добавление имени таблицы в список
                }
                cursor.close()

                // ---------------------------------------------------------
                // 2.3 Проверка наличия всех обязательных таблиц
                // ---------------------------------------------------------
                // Вычисление разницы между требуемыми и существующими таблицами
                val missing = REQUIRED_TABLES - tables

                // Если есть отсутствующие таблицы - ошибка
                if (missing.isNotEmpty()) {
                    DomainDatabaseValidator.ValidationResult.Error(
                        "Missing tables: ${missing.joinToString()}"
                    )
                } else {
                    // Все проверки пройдены успешно
                    DomainDatabaseValidator.ValidationResult.Success
                }
            }
        } catch (e: Exception) {
            // ---------------------------------------------------------
            // 3. Обработка любых исключений (битый файл, не SQLite, ошибка доступа)
            // ---------------------------------------------------------
            DomainDatabaseValidator.ValidationResult.Error(
                "Database validation failed: ${e.message}"
            )
        }
    }
}