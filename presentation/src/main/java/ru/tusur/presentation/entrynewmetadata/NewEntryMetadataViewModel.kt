package ru.tusur.presentation.entrynewmetadata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.tusur.core.util.ValidationUtils
import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Year
import ru.tusur.domain.usecase.reference.*

class NewEntryMetadataViewModel(
    getYears: GetYearsUseCase,
    getBrands: GetBrandsUseCase,
    private val getModelsForBrandAndYear: GetModelsForBrandAndYearUseCase,
    getLocations: GetLocationsUseCase,
    private val addYear: AddYearUseCase,
    private val addBrand: AddBrandUseCase,
    private val addModel: AddModelUseCase,
    private val addLocation: AddLocationUseCase,
    private val deleteYearUseCase: DeleteYearUseCase,
    private val deleteBrandUseCase: DeleteBrandUseCase,
    private val deleteModelUseCase: DeleteModelUseCase,
    private val deleteLocationUseCase: DeleteLocationUseCase

) : ViewModel() {

    data class UiState(
        val entryId: Long? = null,
        val isContinueEnabled: Boolean = false,

        val years: List<Year> = emptyList(),
        val brands: List<Brand> = emptyList(),
        val models: List<Model> = emptyList(),
        val locations: List<Location> = emptyList(),

        val selectedYear: Year? = null,
        val selectedBrand: Brand? = null,
        val selectedModel: Model? = null,
        val selectedLocation: Location? = null,

        val newYearInput: String = "",
        val newBrandInput: String = "",
        val newModelInput: String = "",
        val newLocationInput: String = "",

        val title: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // Reactive brand/year selections
    private val selectedBrand = MutableStateFlow<Brand?>(null)
    private val selectedYear = MutableStateFlow<Year?>(null)

    // Reactive filtered models
    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredModelsFlow: Flow<List<Model>> =
        combine(selectedBrand, selectedYear) { brand, year ->
            if (brand == null || year == null) null else brand to year
        }.flatMapLatest { pair ->
            if (pair == null) flowOf(emptyList())
            else getModelsForBrandAndYear(pair.first, pair.second)
        }

    init {
        combine(
            getYears(),
            getBrands(),
            filteredModelsFlow,
            getLocations()
        ) { years, brands, models, locations ->
            _uiState.value = _uiState.value.copy(
                years = years,
                brands = brands,
                models = models,
                locations = locations
            )
        }.launchIn(viewModelScope)
    }

    // -------------------------
    // SELECTION HANDLERS
    // -------------------------

    fun onYearSelected(year: Year?) {
        selectedYear.value = year
        _uiState.value = _uiState.value.copy(
            selectedYear = year,
            selectedModel = null // reset model when year changes
        )
        updateContinueButton()
    }

    fun onBrandSelected(brand: Brand?) {
        selectedBrand.value = brand
        _uiState.value = _uiState.value.copy(
            selectedBrand = brand,
            selectedModel = null // reset model when brand changes
        )
        updateContinueButton()
    }

    fun onModelSelected(model: Model?) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
        updateContinueButton()
    }

    fun onLocationSelected(location: Location?) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
        updateContinueButton()
    }

    // -------------------------
    // INPUT HANDLERS
    // -------------------------

    fun onNewYearInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(newYearInput = text)
    }

    fun onNewBrandInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(newBrandInput = text)
    }

    fun onNewModelInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(newModelInput = text)
    }

    fun onNewLocationInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(newLocationInput = text)
    }

    fun onTitleChanged(text: String) {
        _uiState.value = _uiState.value.copy(title = text)
        updateContinueButton()
    }

    // -------------------------
    // ADD NEW METADATA
    // -------------------------

    fun addNewYear() {
        val input = _uiState.value.newYearInput

        ValidationUtils.validateYear(input).onSuccess { yearInt ->
            viewModelScope.launch {
                addYear(Year(yearInt)).onSuccess {
                    val year = Year(yearInt)
                    selectedYear.value = year
                    _uiState.value = _uiState.value.copy(
                        newYearInput = "",
                        selectedYear = year,
                        selectedModel = null
                    )
                }
            }
        }
    }

    fun addNewBrand() {
        val input = ValidationUtils.maxLengthTrim(_uiState.value.newBrandInput, 30)
        if (input.isEmpty()) return

        val brand = Brand(input)

        viewModelScope.launch {
            addBrand(brand).onSuccess {
                selectedBrand.value = brand
                _uiState.value = _uiState.value.copy(
                    newBrandInput = "",
                    selectedBrand = brand,
                    selectedModel = null
                )
            }
        }
    }

    fun addNewModel() {
        val input = _uiState.value.newModelInput.trim()
        val brand = _uiState.value.selectedBrand ?: return
        val year = _uiState.value.selectedYear ?: return
        if (input.isEmpty()) return

        val model = Model(input, brand, year)

        viewModelScope.launch {
            addModel(model).onSuccess {
                _uiState.value = _uiState.value.copy(
                    newModelInput = "",
                    models = _uiState.value.models + model,
                    selectedModel = model
                )
            }
        }
    }

    fun addNewLocation() {
        val input = ValidationUtils.maxLengthTrim(_uiState.value.newLocationInput, 15)
        if (input.isEmpty()) return

        val location = Location(input)

        viewModelScope.launch {
            addLocation(location).onSuccess {
                _uiState.value = _uiState.value.copy(
                    newLocationInput = "",
                    selectedLocation = location
                )
            }
        }
    }

    // -------------------------
    // DELETE LOGIC
    // -------------------------


    fun deleteYear(year: Year) {
        viewModelScope.launch {
            deleteYearUseCase(year)
        }
    }

    fun deleteBrand(brand: Brand) {
        viewModelScope.launch {
            deleteBrandUseCase(brand)
        }
    }

    fun deleteModel(model: Model) {
        viewModelScope.launch {
            deleteModelUseCase(model)
        }
    }

    fun deleteLocation(location: Location) {
        viewModelScope.launch {
            deleteLocationUseCase(location)
        }
    }


    // -------------------------
    // CONTINUE BUTTON LOGIC
    // -------------------------

    private fun updateContinueButton() {
        val state = _uiState.value
        _uiState.value = state.copy(
            isContinueEnabled =
                state.selectedYear != null &&
                        state.selectedBrand != null &&
                        state.selectedModel != null &&
                        state.selectedLocation != null &&
                        ValidationUtils.nonEmpty(state.title)
        )
    }
}
