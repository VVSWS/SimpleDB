package ru.tusur.presentation.util

import android.content.Context
import ru.tusur.presentation.R

interface StringProvider {
    fun get(id: Int, vararg args: Any): String

    fun databaseCreated(): String
    fun databaseCreateError(): String
    fun databaseDeleted(): String
    fun databaseDeleteError(): String
}

class AndroidStringProvider(
    private val context: Context
) : StringProvider {

    override fun get(id: Int, vararg args: Any): String =
        context.getString(id, *args)

    override fun databaseCreated(): String =
        context.getString(R.string.db_created)

    override fun databaseCreateError(): String =
        context.getString(R.string.db_create_failed)

    override fun databaseDeleted(): String =
        context.getString(R.string.settings_db_delete_success)

    override fun databaseDeleteError(): String =
        context.getString(R.string.settings_db_delete_failed)
}
