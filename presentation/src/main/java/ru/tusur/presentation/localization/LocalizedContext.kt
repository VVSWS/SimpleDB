package ru.tusur.presentation.localization

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

class LocalizedContext(base: Context, locale: Locale) : ContextWrapper(base) {

    private val localizedContext: Context = run {
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        base.createConfigurationContext(config)
    }

    override fun getResources() = localizedContext.resources
    override fun getAssets() = localizedContext.assets
    override fun getTheme() = localizedContext.theme
}

fun Context.withLocale(locale: Locale): Context =
    LocalizedContext(this, locale)
