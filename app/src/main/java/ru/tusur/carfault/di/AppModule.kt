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
import ru.tusur.data.local.MergeDatabaseManager
import ru.tusur.data.local.RoomDatabaseValidator
import ru.tusur.data.local.database.AppDatabase
import ru.tusur.data.local.database.dao.*
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
import ru.tusur.presentation.entryview.RecordingViewViewModel
import ru.tusur.presentation.search.SharedSearchViewModel



// DataStore delegate
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val appModule = module {

    /* ================
     *  CORE
     * ================ */

    single { FileHelper }
    single<DatabaseValidator> { RoomDatabaseValidator() }
    single<DataStore<Preferences>> { androidContext().dataStore }

    /* ================
     *  DATABASE
     * ================ */

    // One provider ONLY
    single { DatabaseProvider(androidContext()) }

    // Merge manager
    single { MergeDatabaseManager(androidContext(), get()) }

    // Active DB file
    single<File>(named("activeDbFile")) {
        FileHelper.getActiveDatabaseFile(androidContext())
    }

    // AppDatabase bound to active file
    single<AppDatabase> {
        val dbFile: File = get(named("activeDbFile"))
        get<DatabaseProvider>().getDatabase(dbFile)
    }

    /* ================
     *  DAOs
     * ================ */

    single<EntryDao> { get<AppDatabase>().entryDao() }
    single<YearDao> { get<AppDatabase>().yearDao() }
    single<ModelDao> { get<AppDatabase>().modelDao() }
    single<LocationDao> { get<AppDatabase>().locationDao() }
    single<EntryImageDao> { get<AppDatabase>().entryImageDao() }

    /* ================
     *  MAPPERS
     * ================ */

    single { EntryMapper() }
    single { ReferenceDataMapper() }

    /* ================
     *  REPOSITORIES
     * ================ */

    single<FaultRepository> { DefaultFaultRepository(get(), get()) }
    single<ReferenceDataRepository> { DefaultReferenceDataRepository(get(), get(), get(), get()) }

    /* ================
     *  USE CASES
     * ================ */

    single { CreateDatabaseUseCase(androidContext()) }
    single { OpenDatabaseUseCase(androidContext()) }
    single { GetCurrentDatabaseInfoUseCase(androidContext()) }

    factory { GetEntriesUseCase(get()) }
    factory { GetEntryByIdUseCase(get()) }
    factory { CreateEntryUseCase(get()) }
    factory { UpdateEntryUseCase(get()) }
    factory { DeleteEntryUseCase(get()) }
    factory { GetRecentEntriesUseCase(get()) }
    factory { SearchEntriesUseCase(get()) }

    factory { GetYearsUseCase(get()) }
    factory { AddYearUseCase(get()) }
    factory { GetModelsUseCase(get()) }
    factory { AddModelUseCase(get()) }
    factory { GetLocationsUseCase(get()) }
    factory { AddLocationUseCase(get()) }

    /* ================
     *  VIEWMODELS
     * ================ */

    viewModel { MainViewModel(get()) }

    viewModel {
        EntryListViewModel(
            getRecentEntriesUseCase = get(),
            searchEntriesUseCase = get(),
            deleteEntryUseCase = get()
        )
    }

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

    viewModel {
        EditEntryViewModel(
            getEntryById = get(),
            createEntry = get(),
            updateEntry = get(),
            deleteEntry = get()
        )
    }

    viewModel {
        EntrySearchViewModel(
            getYears = get(),
            getModels = get(),
            getLocations = get()
        )
    }

    viewModel { (id: Long) -> RecordingViewViewModel(get<FaultRepository>(), id) }

    viewModel { SharedSearchViewModel() }


    viewModel {
        SettingsViewModel(
            context = androidContext(),
            dataStore = get(),
            createDbUseCase = get(),
            openDbUseCase = get(),
            mergeManager = get(),
            provider = get()
        )
    }

    viewModel { AboutViewModel() }
}
