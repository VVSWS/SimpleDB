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
import ru.tusur.data.local.DatabaseMergeRepositoryImpl
import ru.tusur.data.local.database.AppDatabase
import ru.tusur.data.local.database.dao.*
import ru.tusur.data.mapper.EntryMapper
import ru.tusur.data.mapper.ReferenceDataMapper
import ru.tusur.data.repository.DefaultFaultRepository
import ru.tusur.data.repository.DefaultReferenceDataRepository
import ru.tusur.domain.repository.DatabaseMergeRepository
import ru.tusur.domain.repository.DatabaseValidator
import ru.tusur.domain.repository.FaultRepository
import ru.tusur.domain.repository.ReferenceDataRepository
import ru.tusur.domain.usecase.database.CreateDatabaseUseCase
import ru.tusur.domain.usecase.database.GetCurrentDatabaseInfoUseCase
import ru.tusur.domain.usecase.database.MergeDatabaseUseCase
import ru.tusur.domain.usecase.database.ExportDatabaseUseCase
import ru.tusur.domain.usecase.entry.*
import ru.tusur.domain.usecase.reference.*
import ru.tusur.presentation.about.AboutViewModel
import ru.tusur.presentation.entryedit.EditEntryDescriptionViewModel
import ru.tusur.presentation.entrylist.EntryListViewModel
import ru.tusur.presentation.entrynewmetadata.NewEntryMetadataViewModel
import ru.tusur.presentation.entrysearch.EntrySearchViewModel
import ru.tusur.presentation.mainscreen.MainViewModel
import ru.tusur.presentation.settings.SettingsViewModel
import ru.tusur.presentation.entryview.RecordingViewViewModel
import ru.tusur.presentation.search.SharedSearchViewModel
import java.io.File

// DataStore delegate
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val appModule = module {

    /* ============================
     *  SHARED VIEWMODELS
     * ============================ */
    single { SharedSearchViewModel() }

    /* ============================
     *  CORE
     * ============================ */
    single { FileHelper }
    single<DatabaseValidator> { RoomDatabaseValidator() }
    single<DataStore<Preferences>> { androidContext().dataStore }

    /* ============================
     *  DATABASE
     * ============================ */
    single { DatabaseProvider(androidContext()) }
    single { MergeDatabaseManager(androidContext(), get()) }

    single<File>(named("activeDbFile")) {
        FileHelper.getActiveDatabaseFile(androidContext())
    }

    single<AppDatabase> {
        val dbFile: File = get(named("activeDbFile"))
        get<DatabaseProvider>().getDatabase(dbFile)
    }

    /* ============================
     *  DAOs
     * ============================ */
    single<EntryDao> { get<AppDatabase>().entryDao() }
    single<YearDao> { get<AppDatabase>().yearDao() }
    single<BrandDao> { get<AppDatabase>().brandDao() }
    single<ModelDao> { get<AppDatabase>().modelDao() }
    single<LocationDao> { get<AppDatabase>().locationDao() }
    single<EntryImageDao> { get<AppDatabase>().entryImageDao() }

    /* ============================
     *  MAPPERS
     * ============================ */
    single { EntryMapper() }
    single { ReferenceDataMapper() }

    /* ============================
     *  REPOSITORIES
     * ============================ */
    single<FaultRepository> { DefaultFaultRepository(get(), get(), get()) }
    single<ReferenceDataRepository> { DefaultReferenceDataRepository(get(), get(), get(), get(), get()) }

    // Merge repository (domain interface → data implementation)
    single<DatabaseMergeRepository> {
        DatabaseMergeRepositoryImpl(androidContext(), get())
    }

    /* ============================
     *  USE CASES
     * ============================ */

    // Database
    single { CreateDatabaseUseCase(androidContext()) }
    single { GetCurrentDatabaseInfoUseCase(androidContext()) }
    factory { MergeDatabaseUseCase(get()) }
    factory { ExportDatabaseUseCase(androidContext()) }

    // Entry
    factory { GetEntriesUseCase(get()) }
    factory { GetEntryByIdUseCase(get()) }
    factory { CreateEntryUseCase(get()) }
    factory { UpdateEntryUseCase(get()) }
    factory { DeleteEntryUseCase(get()) }
    factory { GetRecentEntriesUseCase(get()) }
    factory { SearchEntriesUseCase(get()) }
    factory { GetModelsForBrandAndYearUseCase(get()) }

    // Reference data
    factory { GetYearsUseCase(get()) }
    factory { AddYearUseCase(get()) }
    factory { AddModelUseCase(get()) }
    factory { GetBrandsUseCase(get()) }
    factory { AddBrandUseCase(get()) }
    factory { GetLocationsUseCase(get()) }
    factory { AddLocationUseCase(get()) }

    /* ============================
     *  VIEWMODELS
     * ============================ */

    viewModel { MainViewModel(get()) }

    viewModel {
        EntryListViewModel(
            getRecentEntriesUseCase = get(),
            searchEntriesUseCase = get(),
            deleteEntryUseCase = get()
        )
    }

    viewModel {
        EntrySearchViewModel(
            getYears = get(),
            getBrands = get(),
            getModelsForBrandAndYear = get(),
            getLocations = get()
        )
    }

    viewModel {
        NewEntryMetadataViewModel(
            getYears = get(),
            getBrands = get(),
            getModelsForBrandAndYear = get(),
            getLocations = get(),
            addYear = get(),
            addBrand = get(),
            addModel = get(),
            addLocation = get()
        )
    }

    viewModel {
        EditEntryDescriptionViewModel(
            getEntryById = get(),
            createEntry = get(),
            updateEntry = get(),
            deleteEntry = get()
        )
    }

    viewModel { (id: Long) -> RecordingViewViewModel(get<FaultRepository>(), id) }

    viewModel {
        SettingsViewModel(
            context = androidContext(),
            dataStore = get(),
            createDbUseCase = get(),
            mergeDbUseCase = get(),
            exportDbUseCase = get(),
            provider = get()
        )
    }

    viewModel { AboutViewModel() }
}
