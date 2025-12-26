package ru.tusur.presentation.entrysearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Year
import ru.tusur.domain.usecase.reference.*

class EntrySearchViewModel(
    getYears: GetYearsUseCase,
    getModels: GetModelsUseCase,
    getLocations: GetLocationsUseCase
) : ViewModel() {

    data class UiState(
        val years: List<Year> = emptyList(),
        val models: List<Model> = emptyList(),
        val locations: List<Location> = emptyList(),

        val selectedYear: Year? = null,
        val selectedModel: Model? = null,
        val selectedLocation: Location? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
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
        }.launchIn(viewModelScope)
    }

    fun onYearSelected(year: Year?) {
        _uiState.value = _uiState.value.copy(selectedYear = year)
    }

    fun onModelSelected(model: Model?) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
    }

    fun onLocationSelected(location: Location?) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
    }

    fun buildFilter(): Filter {
        return Filter(
            year = _uiState.value.selectedYear?.value,
            model = _uiState.value.selectedModel?.name,
            location = _uiState.value.selectedLocation?.name
        )
    }

    data class Filter(
        val year: String? = null,
        val model: String? = null,
        val location: String? = null
    )
}
