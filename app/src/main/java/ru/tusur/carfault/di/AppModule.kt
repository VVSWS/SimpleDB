package ru.tusur.carfault.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import androidx.datastore.preferences.core.Preferences
import ru.tusur.core.util.FileHelper
import ru.tusur.data.backup.ExportImagesUseCase
import ru.tusur.data.backup.MergeJsonDatabaseUseCase
import ru.tusur.data.local.DatabaseProvider
import ru.tusur.data.local.DatabaseMergeRepositoryImpl
import ru.tusur.data.local.MergeDatabaseManager
import ru.tusur.data.local.RoomDatabaseValidator
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
import ru.tusur.data.backup.ExportDatabaseUseCase
import ru.tusur.data.backup.ImportJsonDatabaseUseCase
import ru.tusur.domain.usecase.database.GetCurrentDatabaseInfoUseCase
import ru.tusur.domain.usecase.entry.*
import ru.tusur.domain.usecase.reference.*
import ru.tusur.presentation.about.AboutViewModel
import ru.tusur.presentation.entryedit.EditEntryDescriptionViewModel
import ru.tusur.presentation.entrylist.EntryListViewModel
import ru.tusur.presentation.entrynewmetadata.NewEntryMetadataViewModel
import ru.tusur.presentation.entrysearch.EntrySearchViewModel
import ru.tusur.presentation.entryview.RecordingViewViewModel
import ru.tusur.presentation.mainscreen.MainViewModel
import ru.tusur.presentation.search.SharedSearchViewModel
import ru.tusur.presentation.settings.SettingsViewModel
import ru.tusur.presentation.util.StringProvider
import java.io.File
import ru.tusur.presentation.util.AndroidStringProvider
import ru.tusur.domain.repository.DatabaseRepository
import ru.tusur.data.repository.DatabaseRepositoryImpl


//val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val Context.dataStore by preferencesDataStore(name = "settings")


val appModule = module {

    // Shared ViewModels
    single { SharedSearchViewModel() }

    // Core
    single { FileHelper }
    single<DatabaseValidator> { RoomDatabaseValidator() }
    single<DataStore<Preferences>> { androidContext().dataStore }

    // Database
    single { DatabaseProvider(androidContext()) }
    single { MergeDatabaseManager(androidContext(), get()) }
    single { ExportImagesUseCase(get()) }


    single<File>(named("activeDbFile")) {
        FileHelper.getActiveDatabaseFile(androidContext())
    }

    single<AppDatabase> {
        val dbFile: File = get(named("activeDbFile"))
        get<DatabaseProvider>().getDatabase(dbFile)
    }
    //UTIL
    single<StringProvider> { AndroidStringProvider(androidContext()) }


    // DAOs
    single<EntryDao> { get<AppDatabase>().entryDao() }
    single<YearDao> { get<AppDatabase>().yearDao() }
    single<BrandDao> { get<AppDatabase>().brandDao() }
    single<ModelDao> { get<AppDatabase>().modelDao() }
    single<LocationDao> { get<AppDatabase>().locationDao() }
    single<EntryImageDao> { get<AppDatabase>().entryImageDao() }

    // Mappers
    single { EntryMapper() }
    single { ReferenceDataMapper() }

    // Repository
    single<DatabaseRepository> { DatabaseRepositoryImpl(provider = get()) }

    // Use case
    single { MergeJsonDatabaseUseCase(context = get(), db = get()) }

    // Repositories
    single<FaultRepository> { DefaultFaultRepository( appContext = get(), entryDao = get(), imageDao = get(), mapper = get() ) }
    single<ReferenceDataRepository> {
        DefaultReferenceDataRepository(get(), get(), get(), get(), get())
    }

    single<DatabaseMergeRepository> {
        DatabaseMergeRepositoryImpl(androidContext(), get())
    }

    // Use cases - Database
    single { CreateDatabaseUseCase(androidContext()) }
    single { GetCurrentDatabaseInfoUseCase(androidContext(), get<FaultRepository>()) }
    factory { ExportDatabaseUseCase(androidContext(), get()) }
    factory { ImportJsonDatabaseUseCase(androidContext(), get()) }
    factory { MergeJsonDatabaseUseCase(androidContext(), get()) }


    // Use cases - Entry
    factory { GetEntriesUseCase(get()) }
    factory { GetEntryByIdUseCase(get()) }
    factory { CreateEntryUseCase(get()) }
    factory { UpdateEntryUseCase(get()) }
    factory { DeleteEntryUseCase(get()) }
    factory { GetRecentEntriesUseCase(get()) }
    factory { SearchEntriesUseCase(get()) }
    factory { GetModelsForBrandAndYearUseCase(get()) }

    // Use cases - Reference data
    factory { GetYearsUseCase(get()) }
    factory { AddYearUseCase(get()) }
    factory { AddModelUseCase(get()) }
    factory { GetBrandsUseCase(get()) }
    factory { AddBrandUseCase(get()) }
    factory { GetLocationsUseCase(get()) }
    factory { AddLocationUseCase(get()) }

    // Use cases - Delete reference
    factory { DeleteBrandUseCase(get()) }
    factory { DeleteModelUseCase(get()) }
    factory { DeleteLocationUseCase(get()) }
    factory { DeleteYearUseCase(get()) }
    factory { DeleteImageUseCase(get()) }


    // ViewModels
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
            addLocation = get(),
            deleteYearUseCase = get(),
            deleteBrandUseCase = get(),
            deleteModelUseCase = get(),
            deleteLocationUseCase = get(),
            createEntryUseCase = get()
        )
    }

    viewModel {
        EditEntryDescriptionViewModel(
            getEntryById = get(),
            createEntry = get(),
            updateEntry = get(),
            deleteEntryUseCase = get(),
            deleteImageUseCase = get()
        )
    }


    viewModel { (id: Long) -> RecordingViewViewModel(get<FaultRepository>(), id) }

    viewModel {
        SettingsViewModel(
            dataStore = get(),
            createDbUseCase = get(),
            mergeDbUseCase = get(),
            exportDbUseCase = get(),
            provider = get(),
            strings = get()
        )
    }

    viewModel { AboutViewModel() }
}
