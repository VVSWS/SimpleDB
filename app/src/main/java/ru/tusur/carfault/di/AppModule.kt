package ru.tusur.carfault.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.tusur.core.util.FileHelper
import ru.tusur.data.backup.ExportDatabaseUseCase
import ru.tusur.data.backup.ExportImagesUseCase
import ru.tusur.data.backup.ImportJsonDatabaseUseCase
import ru.tusur.data.backup.MergeJsonDatabaseUseCase
import ru.tusur.data.local.DatabaseProvider
import ru.tusur.data.local.RoomDatabaseValidator
import ru.tusur.data.local.database.AppDatabase
import ru.tusur.data.mapper.EntryMapper
import ru.tusur.data.mapper.ReferenceDataMapper
import ru.tusur.data.repository.DefaultFaultRepository
import ru.tusur.data.repository.DefaultReferenceDataRepository
import ru.tusur.data.usecase.CreateDatabaseUseCaseImpl
import ru.tusur.data.usecase.GetCurrentDatabaseInfoUseCase
import ru.tusur.domain.repository.DatabaseMaintenanceRepository
import ru.tusur.domain.repository.DatabaseValidator
import ru.tusur.domain.repository.FaultRepository
import ru.tusur.domain.repository.ReferenceDataRepository
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
import ru.tusur.presentation.shared.SharedAppEventsViewModel
import ru.tusur.presentation.util.AndroidStringProvider
import ru.tusur.presentation.util.StringProvider
import ru.tusur.data.repository.DatabaseMaintenanceRepositoryImpl


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val appModule = module {

    // Shared ViewModels
    single { SharedSearchViewModel() }
    single { SharedAppEventsViewModel() }

    // Core helpers
    single { FileHelper }
    single<DatabaseValidator> { RoomDatabaseValidator() }
    single<DataStore<Preferences>> { androidContext().dataStore }
    single<StringProvider> { AndroidStringProvider(androidContext()) }

    // Database provider
    single {
        DatabaseProvider(androidContext())
    }

    single<AppDatabase> {
        get<DatabaseProvider>().getCurrentDatabase()
    }



    single<DatabaseMaintenanceRepository> {
        DatabaseMaintenanceRepositoryImpl(db = get())
    }


    // Mappers
    single { EntryMapper() }
    single { ReferenceDataMapper() }

    // Repositories
    single<FaultRepository> {
        DefaultFaultRepository(
            appContext = androidContext(),
            provider = get(),
            mapper = get()
        )
    }

    single<ReferenceDataRepository> {
        DefaultReferenceDataRepository(
            provider = get(),
            mapper = get()
        )
    }

    // Use Cases — Database / Backup
    single {
        CreateDatabaseUseCaseImpl(
            context = androidContext(),
            provider = get()
        )
    }

    single {
        MergeJsonDatabaseUseCase(
            context = androidContext(),
            provider = get(),
            faultRepository = get(),
            referenceRepository = get()
        )
    }

    single {
        ExportDatabaseUseCase(
            context = androidContext(),
            provider = get(),
            faultRepository = get(),
            referenceRepository = get()
        )
    }

    single {
        ImportJsonDatabaseUseCase(
            context = androidContext(),
            provider = get(),
            faultRepository = get(),
            referenceRepository = get()
        )
    }

    single { ExportImagesUseCase(get()) }

    // NEW snapshot-based DB info use case
    single {
        GetCurrentDatabaseInfoUseCase(
            context = androidContext(),
            provider = get(),
            faultRepository = get()
        )
    }

    // Use Cases — Entry
    factory { GetEntriesUseCase(get()) }
    factory { GetEntryByIdUseCase(get()) }
    factory { CreateEntryUseCase(get()) }
    factory { UpdateEntryUseCase(get()) }
    factory { DeleteEntryUseCase(get()) }
    factory { GetRecentEntriesUseCase(get()) }
    factory { SearchEntriesUseCase(get()) }
    factory { GetModelsForBrandAndYearUseCase(get()) }

    // Use Cases — Reference Data
    factory { GetYearsUseCase(get()) }
    factory { AddYearUseCase(get()) }
    factory { AddModelUseCase(get()) }
    factory { GetBrandsUseCase(get()) }
    factory { AddBrandUseCase(get()) }
    factory { GetLocationsUseCase(get()) }
    factory { AddLocationUseCase(get()) }

    factory { DeleteBrandUseCase(get()) }
    factory { DeleteModelUseCase(get()) }
    factory { DeleteLocationUseCase(get()) }
    factory { DeleteYearUseCase(get()) }
    factory { DeleteImageUseCase(get()) }




    // ViewModels
    viewModel {
        MainViewModel(
            getCurrentDbInfo = get(),
            faultRepository = get(),
            dbMaintenanceRepository = get(),
            sharedEvents = get()
        )
    }


    viewModel {
        EntryListViewModel(
            getRecentEntriesUseCase = get(),
            searchEntriesUseCase = get(),
            getEntryByIdUseCase = get(),
            deleteEntryUseCase = get(),
            sharedEvents = get()
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
            createEntryUseCase = get(),
            sharedEvents = get()
        )
    }

    viewModel { (id: Long) ->
        EditEntryDescriptionViewModel(
            id = id,
            getEntryById = get(),
            createEntry = get(),
            updateEntry = get(),
            deleteEntryUseCase = get(),
            sharedEvents = get()
        )
    }

    viewModel { (id: Long) ->
        RecordingViewViewModel(
            repository = get(),
            getEntryByIdUseCase = get(),
            deleteEntryUseCase = get(),
            sharedEvents = get(),
            entryId = id
        )
    }

    viewModel {
        SettingsViewModel(
            context = androidContext(),
            dataStore = get(),
            mergeDbUseCase = get(),
            exportDbUseCase = get(),
            strings = get(),
            sharedEvents = get()
        )
    }

    viewModel { AboutViewModel() }
}
