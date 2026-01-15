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




class NewEntryMetadataViewModel(
    private val getYears: GetYearsUseCase,
    private val getBrands: GetBrandsUseCase,
    private val getModelsForBrandAndYear: GetModelsForBrandAndYearUseCase,
    private val getLocations: GetLocationsUseCase,
    private val addYear: AddYearUseCase,
    private val addBrand: AddBrandUseCase,
    private val addModel: AddModelUseCase,
    private val addLocation: AddLocationUseCase,
    private val deleteYearUseCase: DeleteYearUseCase,
    private val deleteBrandUseCase: DeleteBrandUseCase,
    private val deleteModelUseCase: DeleteModelUseCase,
    private val deleteLocationUseCase: DeleteLocationUseCase,
    private val createEntryUseCase: CreateEntryUseCase,
    private val sharedEvents: SharedAppEventsViewModel,
    private val stringProvider: StringProvider
) : ViewModel() {

    data class UiState(
        val years: List<Year> = emptyList(),
        val brands: List<Brand> = emptyList(),
        val models: List<Model> = emptyList(),
        val locations: List<Location> = emptyList(),

        val selectedYear: Year? = null,
        val selectedBrand: Brand? = null,
        val selectedModel: Model? = null,
        val selectedLocation: Location? = null,

        val title: String = "",
        val entryId: Long? = null,

        val newYearInput: String = "",
        val newBrandInput: String = "",
        val newModelInput: String = "",
        val newLocationInput: String = "",

        val yearErrorMessage: String? = null
    ) {
        val isContinueEnabled: Boolean
            get() = selectedYear != null &&
                    selectedBrand != null &&
                    selectedModel != null &&
                    selectedLocation != null &&
                    title.isNotBlank()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val years = getYears().first()
            val brands = getBrands().first()
            val locations = getLocations().first()

            _uiState.value = _uiState.value.copy(
                years = years,
                brands = brands,
                locations = locations
            )
        }
    }

    fun onYearSelected(year: Year?) {
        _uiState.value = _uiState.value.copy(
            selectedYear = year,
            yearErrorMessage = null
        )
        reloadModels()
    }


    fun onBrandSelected(brand: Brand?) {
        _uiState.value = _uiState.value.copy(selectedBrand = brand)
        reloadModels()
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

    fun onNewYearInputChanged(input: String) {
        val error = when {
            input.any { !it.isDigit() } ->
                stringProvider.get(R.string.error_only_digits)

            input.length > 4 ->
                stringProvider.get(R.string.error_max_4_digits)

            else -> null
        }


        _uiState.value = _uiState.value.copy(
            newYearInput = input,
            yearErrorMessage = error
        )
    }





    fun onNewBrandInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(newBrandInput = value)
    }

    fun onNewModelInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(newModelInput = value)
    }

    fun onNewLocationInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(newLocationInput = value)
    }

    fun addNewYear() {
        viewModelScope.launch {
            val input = _uiState.value.newYearInput

            if (input.length != 4) {
                _uiState.value = _uiState.value.copy(
                    yearErrorMessage = stringProvider.get(R.string.error_year_must_be_4)
                )
                return@launch
            }

            val yearValue = input.toIntOrNull()
            if (yearValue == null) {
                _uiState.value = _uiState.value.copy(
                    yearErrorMessage = stringProvider.get(R.string.invalid_yeaar)
                )
                return@launch
            }

            val year = Year(yearValue)
            addYear(year)

            val years = getYears().first()

            _uiState.value = _uiState.value.copy(
                years = years,
                selectedYear = year,
                newYearInput = "",
                yearErrorMessage = null
            )
        }
    }



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

    fun addNewModel() {
        viewModelScope.launch {
            val name = _uiState.value.newModelInput
            if (name.isBlank()) return@launch

            val year = _uiState.value.selectedYear ?: return@launch
            val brand = _uiState.value.selectedBrand ?: return@launch

            val model = Model(name = name, brand = brand, year = year)
            addModel(model)

            val models = getModelsForBrandAndYear(brand, year).first()

            _uiState.value = _uiState.value.copy(
                models = models,
                selectedModel = model,
                newModelInput = ""
            )
        }
    }

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
            reloadModels()
        }
    }

    fun deleteLocation(location: Location) {
        viewModelScope.launch {
            deleteLocationUseCase(location)
            _uiState.value = _uiState.value.copy(locations = getLocations().first())
        }
    }

    // ---------------------------------------------------------
    // Create entry and notify main screen
    // ---------------------------------------------------------

    fun createEntry(onCreated: (Long) -> Unit) {
        val state = _uiState.value

        val entry = FaultEntry(
            year = state.selectedYear,
            brand = state.selectedBrand,
            model = state.selectedModel,
            location = state.selectedLocation,
            title = state.title,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            val id = createEntryUseCase(entry)

            _uiState.value = _uiState.value.copy(entryId = id)

            sharedEvents.emit(AppEvent.EntryChanged)
            onCreated(id)

        }
    }
}
