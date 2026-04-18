package ru.tusur.presentation.entrynewmetadata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.tusur.domain.model.*
import ru.tusur.domain.usecase.entry.CreateEntryUseCase
import ru.tusur.domain.usecase.reference.*
import ru.tusur.presentation.R
import ru.tusur.presentation.shared.AppEvent
import ru.tusur.presentation.shared.SharedAppEventsViewModel
import ru.tusur.presentation.util.StringProvider

// ---------------------------------------------------------
// ViewModel для экрана ввода метаданных новой записи
// ---------------------------------------------------------
// Управляет выбором и добавлением справочных данных (годы, марки, модели, локации)
// Выполняет валидацию ввода и создаёт черновик записи
class NewEntryMetadataViewModel(
    private val getYears: GetYearsUseCase,                              // Получение списка годов
    private val getBrands: GetBrandsUseCase,                            // Получение списка марок
    private val getModelsForBrandAndYear: GetModelsForBrandAndYearUseCase, // Получение моделей по марке и году
    private val getLocations: GetLocationsUseCase,                      // Получение списка локаций
    private val addYear: AddYearUseCase,                                // Добавление нового года
    private val addBrand: AddBrandUseCase,                              // Добавление новой марки
    private val addModel: AddModelUseCase,                              // Добавление новой модели
    private val addLocation: AddLocationUseCase,                        // Добавление новой локации
    private val deleteYearUseCase: DeleteYearUseCase,                   // Удаление года
    private val deleteBrandUseCase: DeleteBrandUseCase,                 // Удаление марки
    private val deleteModelUseCase: DeleteModelUseCase,                 // Удаление модели
    private val deleteLocationUseCase: DeleteLocationUseCase,           // Удаление локации
    private val createEntryUseCase: CreateEntryUseCase,                 // Создание записи
    private val sharedEvents: SharedAppEventsViewModel,                 // Общие события для уведомлений
    private val stringProvider: StringProvider                          // Провайдер строковых ресурсов
) : ViewModel() {

    // ---------------------------------------------------------
    // UI состояние экрана
    // ---------------------------------------------------------
    data class UiState(
        // Списки справочных данных
        val years: List<Year> = emptyList(),
        val brands: List<Brand> = emptyList(),
        val models: List<Model> = emptyList(),
        val locations: List<Location> = emptyList(),

        // Выбранные значения
        val selectedYear: Year? = null,
        val selectedBrand: Brand? = null,
        val selectedModel: Model? = null,
        val selectedLocation: Location? = null,

        // Заголовок записи и ID созданной записи
        val title: String = "",
        val entryId: Long? = null,

        // Поля для ввода новых значений
        val newYearInput: String = "",
        val newBrandInput: String = "",
        val newModelInput: String = "",
        val newLocationInput: String = "",

        // Ошибки валидации
        val yearErrorMessage: String? = null
    ) {
        // Флаг активности кнопки "Продолжить"
        val isContinueEnabled: Boolean
            get() = selectedYear != null &&
                    selectedBrand != null &&
                    selectedModel != null &&
                    selectedLocation != null &&
                    title.isNotBlank()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // ---------------------------------------------------------
    // Инициализация: загрузка справочных данных
    // ---------------------------------------------------------
    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val years = getYears().first()      // Получение всех годов
            val brands = getBrands().first()    // Получение всех марок
            val locations = getLocations().first()  // Получение всех локаций

            _uiState.value = _uiState.value.copy(
                years = years,
                brands = brands,
                locations = locations
            )
        }
    }

    // ---------------------------------------------------------
    // Обработчики выбора элементов
    // ---------------------------------------------------------
    fun onYearSelected(year: Year?) {
        _uiState.value = _uiState.value.copy(
            selectedYear = year,
            yearErrorMessage = null
        )
        reloadModels()  // При смене года обновить список моделей
    }

    fun onBrandSelected(brand: Brand?) {
        _uiState.value = _uiState.value.copy(selectedBrand = brand)
        reloadModels()  // При смене марки обновить список моделей
    }

    fun onModelSelected(model: Model?) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
    }

    fun onLocationSelected(location: Location?) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
    }

    fun onTitleChanged(text: String) {
        _uiState.value = _uiState.value.copy(title = text)
    }

    // ---------------------------------------------------------
    // Обновление списка моделей на основе выбранной марки и года
    // ---------------------------------------------------------
    private fun reloadModels() {
        val year = _uiState.value.selectedYear
        val brand = _uiState.value.selectedBrand

        if (year != null && brand != null) {
            viewModelScope.launch {
                val models = getModelsForBrandAndYear(brand, year).first()
                _uiState.value = _uiState.value.copy(models = models)
            }
        }
    }

    // ---------------------------------------------------------
    // Валидация ввода нового года
    // ---------------------------------------------------------
    fun onNewYearInputChanged(input: String) {
        val error = when {
            input.any { !it.isDigit() } ->                      // Только цифры
                stringProvider.get(R.string.error_only_digits)

            input.length > 4 ->                                  // Не более 4 цифр
                stringProvider.get(R.string.error_max_4_digits)

            else -> null
        }

        _uiState.value = _uiState.value.copy(
            newYearInput = input,
            yearErrorMessage = error
        )
    }

    // ---------------------------------------------------------
    // Обработчики ввода новых значений
    // ---------------------------------------------------------
    fun onNewBrandInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(newBrandInput = value)
    }

    fun onNewModelInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(newModelInput = value)
    }

    fun onNewLocationInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(newLocationInput = value)
    }

    // ---------------------------------------------------------
    // Добавление нового года
    // ---------------------------------------------------------
    fun addNewYear() {
        viewModelScope.launch {
            val input = _uiState.value.newYearInput

            // Проверка длины: год должен быть из 4 цифр
            if (input.length != 4) {
                _uiState.value = _uiState.value.copy(
                    yearErrorMessage = stringProvider.get(R.string.error_year_must_be_4)
                )
                return@launch
            }

            // Преобразование в число
            val yearValue = input.toIntOrNull()
            if (yearValue == null) {
                _uiState.value = _uiState.value.copy(
                    yearErrorMessage = stringProvider.get(R.string.invalid_yeaar)
                )
                return@launch
            }

            // Сохранение года в БД
            val year = Year(yearValue)
            addYear(year)

            // Обновление списка годов
            val years = getYears().first()

            _uiState.value = _uiState.value.copy(
                years = years,
                selectedYear = year,
                newYearInput = "",
                yearErrorMessage = null
            )
        }
    }

    // ---------------------------------------------------------
    // Добавление новой марки
    // ---------------------------------------------------------
    fun addNewBrand() {
        viewModelScope.launch {
            val name = _uiState.value.newBrandInput
            if (name.isBlank()) return@launch

            val brand = Brand(name)
            addBrand(brand)

            val brands = getBrands().first()

            _uiState.value = _uiState.value.copy(
                brands = brands,
                selectedBrand = brand,
                newBrandInput = ""
            )
        }
    }

    // ---------------------------------------------------------
    // Добавление новой модели
    // ---------------------------------------------------------
    fun addNewModel() {
        viewModelScope.launch {
            val name = _uiState.value.newModelInput
            if (name.isBlank()) return@launch

            // Для добавления модели нужны выбранные год и марка
            val year = _uiState.value.selectedYear ?: return@launch
            val brand = _uiState.value.selectedBrand ?: return@launch

            val model = Model(name = name, brand = brand, year = year)
            addModel(model)

            // Обновление списка моделей
            val models = getModelsForBrandAndYear(brand, year).first()

            _uiState.value = _uiState.value.copy(
                models = models,
                selectedModel = model,
                newModelInput = ""
            )
        }
    }

    // ---------------------------------------------------------
    // Добавление новой локации
    // ---------------------------------------------------------
    fun addNewLocation() {
        viewModelScope.launch {
            val name = _uiState.value.newLocationInput
            if (name.isBlank()) return@launch

            val location = Location(name)
            addLocation(location)

            val locations = getLocations().first()

            _uiState.value = _uiState.value.copy(
                locations = locations,
                selectedLocation = location,
                newLocationInput = ""
            )
        }
    }

    // ---------------------------------------------------------
    // Удаление справочных элементов
    // ---------------------------------------------------------
    fun deleteYear(year: Year) {
        viewModelScope.launch {
            deleteYearUseCase(year)
            _uiState.value = _uiState.value.copy(years = getYears().first())
        }
    }

    fun deleteBrand(brand: Brand) {
        viewModelScope.launch {
            deleteBrandUseCase(brand)
            _uiState.value = _uiState.value.copy(brands = getBrands().first())
        }
    }

    fun deleteModel(model: Model) {
        viewModelScope.launch {
            deleteModelUseCase(model)
            reloadModels()  // Обновление списка после удаления
        }
    }

    fun deleteLocation(location: Location) {
        viewModelScope.launch {
            deleteLocationUseCase(location)
            _uiState.value = _uiState.value.copy(locations = getLocations().first())
        }
    }

    // ---------------------------------------------------------
    // Создание записи и уведомление об изменении
    // ---------------------------------------------------------
    fun createEntry(onCreated: (Long) -> Unit) {
        val state = _uiState.value

        // Сборка черновика записи из выбранных данных
        val entry = FaultEntry(
            year = state.selectedYear,
            brand = state.selectedBrand,
            model = state.selectedModel,
            location = state.selectedLocation,
            title = state.title,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            // Сохранение записи в БД
            val id = createEntryUseCase(entry)

            // Обновление состояния
            _uiState.value = _uiState.value.copy(entryId = id)

            // Уведомление главного экрана об изменении данных
            sharedEvents.emit(AppEvent.EntryChanged)

            // Callback для навигации на следующий экран
            onCreated(id)
        }
    }
}