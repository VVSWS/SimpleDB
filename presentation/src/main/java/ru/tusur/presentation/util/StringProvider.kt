package ru.tusur.presentation.util

import android.content.Context
import ru.tusur.presentation.R

interface StringProvider {
    fun get(id: Int, vararg args: Any): String

    fun databaseCreated(): String
    fun databaseCreateError(): String
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


}
