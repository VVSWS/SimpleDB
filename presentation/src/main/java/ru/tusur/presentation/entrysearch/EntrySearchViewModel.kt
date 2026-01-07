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

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EntrySearchViewModel(
    private val getYears: GetYearsUseCase,
    private val getBrands: GetBrandsUseCase,
    private val getModelsForBrandAndYear: GetModelsForBrandAndYearUseCase,
    private val getLocations: GetLocationsUseCase
) : ViewModel() {

    data class UiState(
        val years: List<Year> = emptyList(),
        val brands: List<Brand> = emptyList(),
        val models: List<Model> = emptyList(),
        val locations: List<Location> = emptyList(),

        val selectedYear: Year? = null,
        val selectedBrand: Brand? = null,
        val selectedModel: Model? = null,
        val selectedLocation: Location? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        // Load static reference data (years, brands, locations)
        combine(
            getYears(),
            getBrands(),
            getLocations()
        ) { years, brands, locations ->
            _uiState.value = _uiState.value.copy(
                years = years,
                brands = brands,
                locations = locations
            )
        }.launchIn(viewModelScope)

        // Reactively load models when brand or year changes
        combine(
            _uiState.map { it.selectedBrand }.distinctUntilChanged(),
            _uiState.map { it.selectedYear }.distinctUntilChanged()
        ) { brand, year ->
            if (brand != null && year != null) {
                getModelsForBrandAndYear(brand, year)
            } else {
                flowOf(emptyList())
            }
        }
            .flatMapLatest { it }
            .onEach { models ->
                _uiState.value = _uiState.value.copy(
                    models = models,
                    selectedModel = null // reset only when brand/year actually change
                )
            }
            .launchIn(viewModelScope)
    }

    // -------------------------
    // FILTER SELECTION
    // -------------------------

    fun onYearSelected(year: Year?) {
        _uiState.value = _uiState.value.copy(selectedYear = year)
    }

    fun onBrandSelected(brand: Brand?) {
        _uiState.value = _uiState.value.copy(selectedBrand = brand)
    }

    fun onModelSelected(model: Model?) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
    }

    fun onLocationSelected(location: Location?) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
    }

    // -------------------------
    // SEARCH QUERY BUILDER
    // -------------------------

    fun buildSearchQuery(): SearchQuery {
        val state = _uiState.value

        return SearchFilter(
            year = state.selectedYear?.value,
            brand = state.selectedBrand?.name,
            model = state.selectedModel?.name,
            location = state.selectedLocation?.name
        ).toQuery()
    }
}
