package ru.tusur.carfault

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.tusur.carfault.di.appModule

// ---------------------------------------------------------
// Главный класс приложения (наследник Application)
// ---------------------------------------------------------
// Инициализируется первым при запуске приложения
// Используется для настройки глобальных компонентов (DI, логгирование, crashlytics)
class CarFaultApplication : Application() {

    // ---------------------------------------------------------
    // Метод onCreate вызывается при создании приложения
    // ---------------------------------------------------------
    // Выполняется до любого Activity, Service или ContentProvider
    // Идеальное место для инициализации библиотек и фреймворков
    override fun onCreate() {
        super.onCreate()

        // ---------------------------------------------------------
        // Запуск Koin (Dependency Injection фреймворк)
        // ---------------------------------------------------------
        // Регистрация контекста приложения для внедрения зависимостей
        // Android-компоненты (Context, Resources) становятся доступны для DI
        startKoin {
            // Передача контекста приложения в Koin
            // Позволяет инжектить Context, SharedPreferences, DataStore и т.д.
            androidContext(this@CarFaultApplication)

            // Подключение модуля с зависимостями
            // Все репозитории, use cases и ViewModel становятся доступны
            modules(appModule)
        }
    }
}