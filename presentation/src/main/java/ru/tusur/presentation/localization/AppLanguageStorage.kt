package ru.tusur.presentation.localization

import android.content.Context
import java.util.Locale

class AppLanguageStorage(context: Context) {

    private val prefs = context.getSharedPreferences("app_language", Context.MODE_PRIVATE)

    fun save(locale: Locale) {
        prefs.edit()
            .putString("language", locale.language)
            .apply()
    }

    fun get(): Locale {
        val lang = prefs.getString("language", null)
        return if (lang != null) Locale(lang) else Locale.getDefault()
    }
}
