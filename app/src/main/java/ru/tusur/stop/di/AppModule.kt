package ru.tusur.stop.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.tusur.stop.core.util.FileHelper
import ru.tusur.stop.data.local.DatabaseProvider
import ru.tusur.stop.data.local.DatabaseValidator
import ru.tusur.stop.data.local.database.AppDatabase
import ru.tusur.stop.data.local.database.dao.*
import ru.tusur.stop.data.mapper.EntryMapper
import ru.tusur.stop.data.mapper.ReferenceDataMapper
import ru.tusur.stop.data.repository.DefaultFaultRepository
import ru.tusur.stop.data.repository.DefaultReferenceDataRepository
import ru.tusur.stop.domain.repository.FaultRepository
import ru.tusur.stop.domain.repository.ReferenceDataRepository
import ru.tusur.stop.domain.usecase.database.*
import ru.tusur.stop.domain.usecase.entry.*
import ru.tusur.stop.domain.usecase.reference.*
import ru.tusur.stop.presentation.about.AboutViewModel
import ru.tusur.stop.presentation.entryedit.EditEntryViewModel
import ru.tusur.stop.presentation.entrylist.EntryListViewModel
import ru.tusur.stop.presentation.entrynewmetadata.NewEntryMetadataViewModel
import ru.tusur.stop.presentation.entrysearch.EntrySearchViewModel
import ru.tusur.stop.presentation.mainscreen.MainViewModel
import ru.tusur.stop.presentation.settings.SettingsViewModel
import java.io.File

val appModule = module {

    /* ======================
     *  CORE UTILS & HELPERS
     * ====================== */
    single { FileHelper }
    single { DatabaseValidator() }

    /* ======================
     *  DATABASE LAYER
     * ====================== */
    single { DatabaseProvider(androidContext()) }

    // Фабрика для получения AppDatabase по пути — ленивая инициализация
    factory { (dbFile: File) ->
        get<DatabaseProvider>().getDatabase(dbFile)
    }

    // Конкретный экземпляр БД: current.db в private storage
    single(named("activeDbFile")) {
        FileHelper.getDatabasePath(androidContext(), isExternal = false).apply {
            parentFile?.mkdirs()
        }
    }

    single<AppDatabase> {
        get<File>(named("activeDbFile")).let { dbFile ->
            get { dbFile } // вызов фабрики выше
        }
    }

    // DAOs
    single<EntryDao> { get<AppDatabase>().entryDao() }
    single<YearDao> { get<AppDatabase>().yearDao() }
    single<ModelDao> { get<AppDatabase>().modelDao() }
    single<LocationDao> { get<AppDatabase>().locationDao() }
    single<EntryImageDao> { get<AppDatabase>().entryImageDao() }

    /* ======================
     *  MAPPERS
     * ====================== */
    single { EntryMapper() }
    single { ReferenceDataMapper() }

    /* ======================
     *  REPOSITORIES (implement domain interfaces)
     * ====================== */
    single<FaultRepository> {
        DefaultFaultRepository(
            entryDao = get(),
            mapper = get()
        )
    }

    single<ReferenceDataRepository> {
        DefaultReferenceDataRepository(
            yearDao = get(),
            modelDao = get(),
            locationDao = get(),
            mapper = get()
        )
    }

    /* ======================
     *  USE CASES
     * ====================== */

    // Entry Use Cases
    factory { GetEntriesUseCase(get()) }
    factory { GetEntryByIdUseCase(get()) }
    factory { CreateEntryUseCase(get()) }
    factory { UpdateEntryUseCase(get()) }
    factory { DeleteEntryUseCase(get()) }
    factory { GetRecentEntriesUseCase(get()) }
    factory { SearchEntriesUseCase(get()) }

    // Reference Data Use Cases
    factory { GetYearsUseCase(get()) }
    factory { AddYearUseCase(get()) }
    factory { GetModelsUseCase(get()) }
    factory { AddModelUseCase(get()) }
    factory { GetLocationsUseCase(get()) }
    factory { AddLocationUseCase(get()) }

    // Database Use Cases
    factory {
        CreateDatabaseUseCase { isExternal ->
            FileHelper.getDatabasePath(androidContext(), isExternal)
        }
    }
    factory { OpenDatabaseUseCase(get()) }
    factory { GetCurrentDatabaseInfoUseCase(get()) }

    /* ======================
     *  VIEW MODELS
     * ====================== */
    viewModel { MainViewModel(get()) }
    viewModel { EntryListViewModel(get(), get()) }
    viewModel { NewEntryMetadataViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { EditEntryViewModel(get(), get(), get(), get()) }
    viewModel { EntrySearchViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel() }
    viewModel { AboutViewModel() }
}