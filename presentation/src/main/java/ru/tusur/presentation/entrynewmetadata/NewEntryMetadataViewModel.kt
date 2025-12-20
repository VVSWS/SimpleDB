package ru.tusur.presentation.entrynewmetadata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.tusur.stop.core.util.ValidationUtils
import ru.tusur.stop.domain.model.Location
import ru.tusur.stop.domain.model.Model
import ru.tusur.stop.domain.model.Year
import ru.tusur.stop.domain.usecase.reference.*

class NewEntryMetadataViewModel(
    getYears: GetYearsUseCase,
    getModels: GetModelsUseCase,
    getLocations: GetLocationsUseCase,
    private val addYear: AddYearUseCase,
    private val addModel: AddModelUseCase,
    private val addLocation: AddLocationUseCase
) : ViewModel() {

    data class UiState(
        val years: List<Year> = emptyList(),
        val models: List<Model> = emptyList(),
        val locations: List<Location> = emptyList(),
        val selectedYear: Year? = null,
        val selectedModel: Model? = null,
        val selectedLocation: Location? = null,
        val newYearInput: String = "",
        val newModelInput: String = "",
        val newLocationInput: String = "",
        val title: String = "",
        val isContinueEnabled: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                getYears(),
                getModels(),
                getLocations()
            ) { years, models, locations ->
                _uiState.value = _uiState.value.copy(
                    years = years,
                    models = models,
                    locations = locations
                )
            }.collect()
        }
    }

    fun onYearSelected(year: Year) {
        _uiState.value = _uiState.value.copy(selectedYear = year)
        updateContinueButton()
    }

    fun onModelSelected(model: Model) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
        updateContinueButton()
    }

    fun onLocationSelected(location: Location) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
        updateContinueButton()
    }

    fun onNewYearInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(newYearInput = text)
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

    fun addNewYear() {
        val input = _uiState.value.newYearInput
        ValidationUtils.validateYear(input).onSuccess { value ->
            addYear(Year(value)).onSuccess {
                _uiState.value = _uiState.value.copy(
                    newYearInput = "",
                    selectedYear = Year(value)
                )
            }
        }
    }

    fun addNewModel() {
        val input = ValidationUtils.maxLengthTrim(_uiState.value.newModelInput, 30)
        if (input.isNotEmpty()) {
            addModel(Model(input)).onSuccess {
                _uiState.value = _uiState.value.copy(
                    newModelInput = "",
                    selectedModel = Model(input)
                )
            }
        }
    }

    fun addNewLocation() {
        val input = ValidationUtils.maxLengthTrim(_uiState.value.newLocationInput, 15)
        if (input.isNotEmpty()) {
            addLocation(Location(input)).onSuccess {
                _uiState.value = _uiState.value.copy(
                    newLocationInput = "",
                    selectedLocation = Location(input)
                )
            }
        }
    }

    private fun updateContinueButton() {
        val state = _uiState.value
        _uiState.value = state.copy(
            isContinueEnabled = state.selectedYear != null &&
                    state.selectedModel != null &&
                    state.selectedLocation != null &&
                    ValidationUtils.nonEmpty(state.title)
        )
    }
}