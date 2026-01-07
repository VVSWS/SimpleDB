package ru.tusur.carfault

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.tusur.carfault.di.appModule

class CarFaultApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CarFaultApplication)
            modules(appModule)
        }
    }
}
