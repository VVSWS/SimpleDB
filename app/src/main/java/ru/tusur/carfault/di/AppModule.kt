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

// ---------------------------------------------------------
// Расширение Context для доступа к DataStore настроек
// ---------------------------------------------------------
// Создание или получение экземпляра DataStore с именем "settings"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// ---------------------------------------------------------
// Главный DI-модуль приложения (Koin)
// ---------------------------------------------------------
// Определение всех зависимостей: репозитории, use cases, ViewModel
val appModule = module {

    // ---------------------------------------------------------
    // Shared ViewModels (общие между экранами)
    // ---------------------------------------------------------
    // ViewModel для общего состояния поиска
    single { SharedSearchViewModel() }
    // ViewModel для глобальных событий приложения
    single { SharedAppEventsViewModel() }

    // ---------------------------------------------------------
    // Core helpers (вспомогательные утилиты)
    // ---------------------------------------------------------
    // Утилиты для работы с файлами (синглтон)
    single { FileHelper }
    // Валидатор целостности базы данных Room
    single<DatabaseValidator> { RoomDatabaseValidator() }
    // DataStore для хранения настроек пользователя
    single<DataStore<Preferences>> { androidContext().dataStore }
    // Провайдер строковых ресурсов (поддержка локализации)
    single<StringProvider> { AndroidStringProvider(androidContext()) }

    // ---------------------------------------------------------
    // Database provider (провайдер базы данных)
    // ---------------------------------------------------------
    // Управление подключением к БД и версионированием
    single {
        DatabaseProvider(androidContext())
    }

    // ---------------------------------------------------------
    // Текущий экземпляр AppDatabase
    // ---------------------------------------------------------
    // Получение актуальной базы данных через провайдера
    single<AppDatabase> {
        get<DatabaseProvider>().getCurrentDatabase()
    }

    // ---------------------------------------------------------
    // Репозиторий для обслуживания базы данных
    // ---------------------------------------------------------
    // Операции по созданию, резервному копированию и восстановлению БД
    single<DatabaseMaintenanceRepository> {
        DatabaseMaintenanceRepositoryImpl(db = get())
    }

    // ---------------------------------------------------------
    // Mappers (преобразователи данных)
    // ---------------------------------------------------------
    // Преобразование EntryEntity ↔ EntryDomain
    single { EntryMapper() }
    // Преобразование справочных данных (марки, модели, года, локации)
    single { ReferenceDataMapper() }

    // ---------------------------------------------------------
    // Repositories (репозитории)
    // ---------------------------------------------------------
    // Репозиторий для работы с записями о неисправностях
    single<FaultRepository> {
        DefaultFaultRepository(
            appContext = androidContext(),
            provider = get(),
            mapper = get()
        )
    }

    // Репозиторий для работы со справочными данными
    single<ReferenceDataRepository> {
        DefaultReferenceDataRepository(
            provider = get(),
            mapper = get()
        )
    }

    // ---------------------------------------------------------
    // Use Cases — Database / Backup (работа с БД и резервное копирование)
    // ---------------------------------------------------------
    // Создание новой базы данных
    single {
        CreateDatabaseUseCaseImpl(
            context = androidContext(),
            provider = get()
        )
    }

    // Слияние JSON-базы с текущей
    single {
        MergeJsonDatabaseUseCase(
            context = androidContext(),
            faultRepository = get(),
            referenceRepository = get()
        )
    }

    // Экспорт базы данных в JSON
    single {
        ExportDatabaseUseCase(
            context = androidContext(),
            faultRepository = get()
        )
    }

    // Импорт базы данных из JSON
    single {
        ImportJsonDatabaseUseCase(
            context = androidContext(),
            faultRepository = get(),
            referenceRepository = get()
        )
    }

    // Экспорт изображений в ZIP-архив
    single { ExportImagesUseCase(get()) }

    // ---------------------------------------------------------
    // Получение информации о текущей БД (снапшот)
    // ---------------------------------------------------------
    // Информация о версии, количестве записей и размере БД
    single {
        GetCurrentDatabaseInfoUseCase(
            context = androidContext(),
            provider = get(),
            faultRepository = get()
        )
    }

    // ---------------------------------------------------------
    // Use Cases — Entry (работа с записями)
    // ---------------------------------------------------------
    // Получение всех записей
    factory { GetEntriesUseCase(get()) }
    // Получение записи по ID
    factory { GetEntryByIdUseCase(get()) }
    // Создание новой записи
    factory { CreateEntryUseCase(get()) }
    // Обновление существующей записи
    factory { UpdateEntryUseCase(get()) }
    // Удаление записи
    factory { DeleteEntryUseCase(get()) }
    // Получение последних записей
    factory { GetRecentEntriesUseCase(get()) }
    // Поиск записей по критериям
    factory { SearchEntriesUseCase(get()) }
    // Получение моделей для конкретной марки и года
    factory { GetModelsForBrandAndYearUseCase(get()) }

    // ---------------------------------------------------------
    // Use Cases — Reference Data (справочные данные)
    // ---------------------------------------------------------
    // Получение списка годов
    factory { GetYearsUseCase(get()) }
    // Добавление нового года
    factory { AddYearUseCase(get()) }
    // Добавление новой модели
    factory { AddModelUseCase(get()) }
    // Получение списка марок
    factory { GetBrandsUseCase(get()) }
    // Добавление новой марки
    factory { AddBrandUseCase(get()) }
    // Получение списка локаций
    factory { GetLocationsUseCase(get()) }
    // Добавление новой локации
    factory { AddLocationUseCase(get()) }

    // ---------------------------------------------------------
    // Use Cases — Delete (удаление справочных данных)
    // ---------------------------------------------------------
    // Удаление марки
    factory { DeleteBrandUseCase(get()) }
    // Удаление модели
    factory { DeleteModelUseCase(get()) }
    // Удаление локации
    factory { DeleteLocationUseCase(get()) }
    // Удаление года
    factory { DeleteYearUseCase(get()) }
    // Удаление изображения
    factory { DeleteImageUseCase(get()) }

    // ---------------------------------------------------------
    // ViewModels (модели представления для экранов)
    // ---------------------------------------------------------
    // Главный экран приложения
    viewModel {
        MainViewModel(
            getCurrentDbInfo = get(),
            faultRepository = get(),
            dbMaintenanceRepository = get(),
            sharedEvents = get()
        )
    }

    // Экран со списком записей
    viewModel {
        EntryListViewModel(
            getRecentEntriesUseCase = get(),
            searchEntriesUseCase = get(),
            getEntryByIdUseCase = get(),
            deleteEntryUseCase = get(),
            sharedEvents = get()
        )
    }

    // Экран поиска записей
    viewModel {
        EntrySearchViewModel(
            getYears = get(),
            getBrands = get(),
            getModelsForBrandAndYear = get(),
            getLocations = get()
        )
    }

    // Экран создания новой записи (выбор метаданных)
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
            sharedEvents = get(),
            stringProvider = get()
        )
    }

    // Экран редактирования описания записи
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

    // Экран просмотра аудиозаписи
    viewModel { (id: Long) ->
        RecordingViewViewModel(
            repository = get(),
            getEntryByIdUseCase = get(),
            deleteEntryUseCase = get(),
            sharedEvents = get(),
            entryId = id
        )
    }

    // Экран настроек приложения
    viewModel {
        SettingsViewModel(
            dataStore = get(),
            mergeDbUseCase = get(),
            exportDbUseCase = get(),
            strings = get(),
            sharedEvents = get()
        )
    }

    // Экран "О приложении"
    viewModel { AboutViewModel() }
}