package ru.tusur.carfault.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.tusur.core.util.FileHelper
import ru.tusur.data.local.DatabaseProvider
import ru.tusur.data.local.RoomDatabaseValidator
import ru.tusur.data.local.database.AppDatabase
import ru.tusur.data.local.database.dao.EntryDao
import ru.tusur.data.local.database.dao.EntryImageDao
import ru.tusur.data.local.database.dao.LocationDao
import ru.tusur.data.local.database.dao.ModelDao
import ru.tusur.data.local.database.dao.YearDao
import ru.tusur.data.mapper.EntryMapper
import ru.tusur.data.mapper.ReferenceDataMapper
import ru.tusur.data.repository.DefaultFaultRepository
import ru.tusur.data.repository.DefaultReferenceDataRepository
import ru.tusur.domain.repository.DatabaseValidator
import ru.tusur.domain.repository.FaultRepository
import ru.tusur.domain.repository.ReferenceDataRepository
import ru.tusur.domain.usecase.database.CreateDatabaseUseCase
import ru.tusur.domain.usecase.database.GetCurrentDatabaseInfoUseCase
import ru.tusur.domain.usecase.database.OpenDatabaseUseCase
import ru.tusur.domain.usecase.entry.CreateEntryUseCase
import ru.tusur.domain.usecase.entry.DeleteEntryUseCase
import ru.tusur.domain.usecase.entry.GetEntriesUseCase
import ru.tusur.domain.usecase.entry.GetEntryByIdUseCase
import ru.tusur.domain.usecase.entry.GetRecentEntriesUseCase
import ru.tusur.domain.usecase.entry.SearchEntriesUseCase
import ru.tusur.domain.usecase.entry.UpdateEntryUseCase
import ru.tusur.domain.usecase.reference.AddLocationUseCase
import ru.tusur.domain.usecase.reference.AddModelUseCase
import ru.tusur.domain.usecase.reference.AddYearUseCase
import ru.tusur.domain.usecase.reference.GetLocationsUseCase
import ru.tusur.domain.usecase.reference.GetModelsUseCase
import ru.tusur.domain.usecase.reference.GetYearsUseCase
import ru.tusur.presentation.about.AboutViewModel
import ru.tusur.presentation.entryedit.EditEntryViewModel
import ru.tusur.presentation.entrylist.EntryListViewModel
import ru.tusur.presentation.entrynewmetadata.NewEntryMetadataViewModel
import ru.tusur.presentation.entrysearch.EntrySearchViewModel
import ru.tusur.presentation.mainscreen.MainViewModel
import ru.tusur.presentation.settings.SettingsViewModel
import java.io.File

// Top-level DataStore delegate for the whole app
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val appModule = module {

    /* ================
     *  CORE / UTILITIES
     * ================ */

    // File utilities (BD_ prefix, active DB path, copy helpers, etc.)
    single { FileHelper }

    // Database schema validator (used by domain layer if needed)
    single<DatabaseValidator> { RoomDatabaseValidator() }

    // DataStore (SettingsViewModel)
    single<DataStore<Preferences>> {
        androidContext().dataStore
    }

    /* ================
     *  DATABASE LAYER
     * ================ */

    // Provider responsible for creating / switching Room databases
    single {
        DatabaseProvider(androidContext())
    }

    // Active DB file (hybrid model: always internal, imported from anywhere)
    single<File>(named("activeDbFile")) {
        FileHelper.getActiveDatabaseFile(androidContext())
    }

    // AppDatabase bound to the current active file
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

    /* ============
     *  MAPPERS
     * ============ */

    single { EntryMapper() }
    single { ReferenceDataMapper() }

    /* =============
     *  REPOSITORIES
     * ============= */

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

    /* ============
     *  USE CASES
     * ============ */

    // Entry
    factory { GetEntriesUseCase(get()) }
    factory { GetEntryByIdUseCase(get()) }
    factory { CreateEntryUseCase(get()) }
    factory { UpdateEntryUseCase(get()) }
    factory { DeleteEntryUseCase(get()) }
    factory { GetRecentEntriesUseCase(get()) }
    factory { SearchEntriesUseCase(get()) }

    // Reference data
    factory { GetYearsUseCase(get()) }
    factory { AddYearUseCase(get()) }
    factory { GetModelsUseCase(get()) }
    factory { AddModelUseCase(get()) }
    factory { GetLocationsUseCase(get()) }
    factory { AddLocationUseCase(get()) }

    // Database (hybrid model: active DB in internal, import from anywhere)
    factory { CreateDatabaseUseCase(androidContext()) }
    factory { OpenDatabaseUseCase(androidContext()) }
    factory { GetCurrentDatabaseInfoUseCase(androidContext()) }

    /* ==============
     *  VIEW MODELS
     * ============== */

    // Shows current DB status on the main screen
    viewModel {
        MainViewModel(
            getCurrentDbInfo = get()
        )
    }

    // Entry list + recent / search filters
    viewModel {
        EntryListViewModel(
            getRecentEntries = get(),
            searchEntries = get(),
            deleteEntryUseCase = get()
        )
    }


    // New entry metadata selection + adding new year/model/location
    viewModel {
        NewEntryMetadataViewModel(
            getYears = get(),
            getModels = get(),
            getLocations = get(),
            addYear = get(),
            addModel = get(),
            addLocation = get()
        )
    }

    // Create / edit / delete single entry
    viewModel {
        EditEntryViewModel(
            getEntryById = get(),
            createEntry = get(),
            updateEntry = get(),
            deleteEntry = get()
        )
    }

    // Search UI (filters only, no execution — used by EntryList)
    viewModel {
        EntrySearchViewModel(
            getYears = get(),
            getModels = get(),
            getLocations = get()
        )
    }

    // Settings: language, theme, DB create/open
    viewModel {
        SettingsViewModel(
            context = androidContext(),
            dataStore = get(),
            createDbUseCase = get(),
            openDbUseCase = get()
        )
    }

    // Static info
    viewModel {
        AboutViewModel()
    }
}
