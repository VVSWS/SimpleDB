package ru.tusur.carfault.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.tusur.core.util.FileHelper
import ru.tusur.data.local.DatabaseProvider
import ru.tusur.data.local.database.AppDatabase
import ru.tusur.data.local.database.dao.EntryDao
import ru.tusur.data.local.database.dao.EntryImageDao
import ru.tusur.data.local.database.dao.LocationDao
import ru.tusur.data.local.database.dao.ModelDao
import ru.tusur.data.local.database.dao.YearDao
import ru.tusur.data.local.RoomDatabaseValidator
import ru.tusur.data.mapper.EntryMapper
import ru.tusur.data.mapper.ReferenceDataMapper
import ru.tusur.data.repository.DefaultFaultRepository
import ru.tusur.data.repository.DefaultReferenceDataRepository
import ru.tusur.domain.repository.DatabaseValidator
import ru.tusur.domain.repository.FaultRepository
import ru.tusur.domain.repository.ReferenceDataRepository
import ru.tusur.domain.usecase.database.*
import ru.tusur.domain.usecase.entry.*
import ru.tusur.domain.usecase.reference.*
import ru.tusur.presentation.about.AboutViewModel
import ru.tusur.presentation.entryedit.EditEntryViewModel
import ru.tusur.presentation.entrylist.EntryListViewModel
import ru.tusur.presentation.entrynewmetadata.NewEntryMetadataViewModel
import ru.tusur.presentation.entrysearch.EntrySearchViewModel
import ru.tusur.presentation.mainscreen.MainViewModel
import ru.tusur.presentation.settings.SettingsViewModel
import java.io.File
import org.koin.androidx.viewmodel.dsl.viewModel

val appModule = module {

    /* ======================
     *  CORE UTILS & HELPERS
     * ====================== */
    single { FileHelper }
    single<DatabaseValidator> {
        RoomDatabaseValidator()
    }

    /* ======================
     *  DATABASE LAYER
     * ====================== */
    single { DatabaseProvider(androidContext()) }

    factory { (dbFile: File) ->
        get<DatabaseProvider>().getDatabase(dbFile)
    }

    single<File>(named("activeDbFile")) {
        FileHelper.getDatabasePath(androidContext(), isExternal = false).apply {
            parentFile?.mkdirs()
        }
    }


    single<AppDatabase> {
        val dbFile: File = get(named("activeDbFile"))
        get<DatabaseProvider>().getDatabase(dbFile)
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
     *  REPOSITORIES
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
    // Entry
    factory { GetEntriesUseCase(get()) }
    factory { GetEntryByIdUseCase(get()) }
    factory { CreateEntryUseCase(get()) }
    factory { UpdateEntryUseCase(get()) }
    factory { DeleteEntryUseCase(get()) }
    factory { GetRecentEntriesUseCase(get()) }
    factory { SearchEntriesUseCase(get()) }

    // Reference Data
    factory { GetYearsUseCase(get()) }
    factory { AddYearUseCase(get()) }
    factory { GetModelsUseCase(get()) }
    factory { AddModelUseCase(get()) }
    factory { GetLocationsUseCase(get()) }
    factory { AddLocationUseCase(get()) }

    // Database
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