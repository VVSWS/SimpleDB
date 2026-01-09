package ru.tusur.presentation.util

import android.content.Context

interface StringProvider {
    fun get(id: Int, vararg args: Any): String
}

class AndroidStringProvider(
    private val context: Context
) : StringProvider {
    override fun get(id: Int, vararg args: Any): String =
        context.getString(id, *args)
}
