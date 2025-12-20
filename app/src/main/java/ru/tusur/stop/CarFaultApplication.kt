package ru.tusur.stop

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.tusur.stop.di.appModule

class CarFaultApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // ✅ Инициализация Koin (без kapt!)
        startKoin {
            androidContext(this@CarFaultApplication)
            modules(appModule)
        }
    }

    // ✅ DataStore как singleton через delegated property
    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
}