package ru.tusur.presentation.entrysearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.SearchFilter
import ru.tusur.domain.model.SearchQuery
import ru.tusur.domain.model.Year
import ru.tusur.domain.model.toQuery
import ru.tusur.domain.usecase.reference.*

// ---------------------------------------------------------
// ViewModel для экрана расширенного поиска
// ---------------------------------------------------------
// Загружает справочные данные (годы, марки, локации)
// Динамически обновляет список моделей при изменении марки или года
// Формирует SearchQuery на основе выбранных фильтров
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EntrySearchViewModel(
    private val getYears: GetYearsUseCase,                           // UseCase для получения годов
    private val getBrands: GetBrandsUseCase,                         // UseCase для получения марок
    private val getModelsForBrandAndYear: GetModelsForBrandAndYearUseCase, // UseCase для получения моделей
    private val getLocations: GetLocationsUseCase                    // UseCase для получения локаций
) : ViewModel() {

    // ---------------------------------------------------------
    // UI состояние экрана
    // ---------------------------------------------------------
    data class UiState(
        val years: List<Year> = emptyList(),          // Список всех годов
        val brands: List<Brand> = emptyList(),        // Список всех марок
        val models: List<Model> = emptyList(),        // Список моделей (фильтруется по марке и году)
        val locations: List<Location> = emptyList(),  // Список всех локаций

        // Выбранные значения фильтров
        val selectedYear: Year? = null,
        val selectedBrand: Brand? = null,
        val selectedModel: Model? = null,
        val selectedLocation: Location? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // ---------------------------------------------------------
    // Инициализация: загрузка статических справочных данных
    // ---------------------------------------------------------
    init {
        // ---------------------------------------------------------
        // Загрузка годов, марок и локаций (не зависят от выбора)
        // ---------------------------------------------------------
        combine(
            getYears(),      // Поток годов
            getBrands(),     // Поток марок
            getLocations()   // Поток локаций
        ) { years, brands, locations ->
            _uiState.value = _uiState.value.copy(
                years = years,
                brands = brands,
                locations = locations
            )
        }.launchIn(viewModelScope)

        // ---------------------------------------------------------
        // Реактивная загрузка моделей при изменении марки или года
        // ---------------------------------------------------------
        combine(
            _uiState.map { it.selectedBrand }.distinctUntilChanged(),  // Отслеживание изменения марки
            _uiState.map { it.selectedYear }.distinctUntilChanged()    // Отслеживание изменения года
        ) { brand, year ->
            // Если выбраны и марка, и год - загружаем модели
            if (brand != null && year != null) {
                getModelsForBrandAndYear(brand, year)
            } else {
                // Иначе возвращаем пустой список
                flowOf(emptyList())
            }
        }
            .flatMapLatest { it }          // Переключение на новый поток при изменении условий
            .onEach { models ->
                _uiState.value = _uiState.value.copy(
                    models = models,
                    selectedModel = null    // Сброс выбранной модели при изменении марки/года
                )
            }
            .launchIn(viewModelScope)
    }

    // ---------------------------------------------------------
    // ОБРАБОТЧИКИ ВЫБОРА ФИЛЬТРОВ
    // ---------------------------------------------------------

    // Выбор года
    fun onYearSelected(year: Year?) {
        _uiState.value = _uiState.value.copy(selectedYear = year)
    }

    // Выбор марки
    fun onBrandSelected(brand: Brand?) {
        _uiState.value = _uiState.value.copy(selectedBrand = brand)
    }

    // Выбор модели
    fun onModelSelected(model: Model?) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
    }

    // Выбор местоположения
    fun onLocationSelected(location: Location?) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
    }

    // ---------------------------------------------------------
    // ПОСТРОЕНИЕ ЗАПРОСА ПОИСКА
    // ---------------------------------------------------------
    // Преобразует выбранные фильтры в SearchQuery для передачи в репозиторий
    fun buildSearchQuery(): SearchQuery {
        val state = _uiState.value

        // Создание SearchFilter из выбранных значений
        return SearchFilter(
            year = state.selectedYear?.value,           // Извлечение числового значения года
            brand = state.selectedBrand?.name,          // Извлечение названия марки
            model = state.selectedModel?.name,          // Извлечение названия модели
            location = state.selectedLocation?.name     // Извлечение названия локации
        ).toQuery()  // Преобразование в SearchQuery (расширение)
    }
}