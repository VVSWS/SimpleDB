package ru.tusur.presentation.entryedit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import ru.tusur.core.files.savePickedImage
import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.FaultEntry
import ru.tusur.domain.model.Location
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Year
import ru.tusur.domain.usecase.entry.*
import ru.tusur.domain.usecase.reference.*

class EditEntryViewModel(
    private val getEntryById: GetEntryByIdUseCase,
    private val createEntry: CreateEntryUseCase,
    private val updateEntry: UpdateEntryUseCase,
    private val deleteEntry: DeleteEntryUseCase,

    private val getYears: GetYearsUseCase,
    private val getBrands: GetBrandsUseCase,
    private val getModels: GetModelsUseCase,
    private val getLocations: GetLocationsUseCase,

    private val addYear: AddYearUseCase,
    private val addBrand: AddBrandUseCase,
    private val addModel: AddModelUseCase,
    private val addLocation: AddLocationUseCase
) : ViewModel() {

    data class UiState(
        val entry: FaultEntry = FaultEntry(),

        val years: List<Year> = emptyList(),
        val brands: List<Brand> = emptyList(),
        val models: List<Model> = emptyList(),
        val locations: List<Location> = emptyList(),

        val newYearInput: String = "",
        val newBrandInput: String = "",
        val newModelInput: String = "",
        val newLocationInput: String = "",

        val isEditMode: Boolean = false,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val saveCompleted: Boolean = false,
        val descriptionError: String? = null
    ) {
        val isValid: Boolean
            get() = descriptionError == null && entry.description.isNotBlank()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        combine(
            getYears(),
            getBrands(),
            getModels(),
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

    fun loadEntry(entryId: Long?) {
        if (entryId == null) {
            _uiState.value = UiState(
                entry = FaultEntry(),
                isEditMode = false,
                isLoading = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            getEntryById(entryId)
                .onSuccess { entry ->
                    _uiState.value = _uiState.value.copy(
                        entry = entry ?: FaultEntry(),
                        isEditMode = entry != null,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = UiState(
                        entry = FaultEntry(),
                        isEditMode = false,
                        isLoading = false
                    )
                }
        }
    }

    // -------------------------
    // FIELD UPDATES
    // -------------------------

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(title = title)
        )
    }

    fun onDescriptionChanged(description: String) {
        val error = if (description.isBlank()) "Description cannot be empty" else null

        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(description = description),
            descriptionError = error
        )
    }

    fun onYearChanged(year: Year) {
        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(year = year)
        )
    }

    fun onBrandChanged(brand: Brand) {
        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(brand = brand)
        )
    }

    fun onModelChanged(model: Model) {
        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(model = model)
        )
    }

    fun onLocationChanged(location: Location) {
        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(location = location)
        )
    }

    // -------------------------
    // IMAGE HANDLING (Option 3)
    // -------------------------

    fun onImagesSelected(context: Context, uris: List<Uri>) {
        val newRelativePaths = uris.map { savePickedImage(context, it) }

        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(
                imageUris = _uiState.value.entry.imageUris + newRelativePaths
            )
        )
    }

    // -------------------------
    // ADD NEW METADATA
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

    fun addNewYear() {
        val input = _uiState.value.newYearInput.trim()
        if (input.isEmpty()) return

        viewModelScope.launch {
            addYear(Year(input.toInt()))
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        newYearInput = "",
                        entry = _uiState.value.entry.copy(year = Year(input.toInt()))
                    )
                }
        }
    }

    fun addNewBrand() {
        val input = _uiState.value.newBrandInput.trim()
        if (input.isEmpty()) return

        viewModelScope.launch {
            addBrand(Brand(input))
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        newBrandInput = "",
                        entry = _uiState.value.entry.copy(brand = Brand(input))
                    )
                }
        }
    }

    fun addNewModel() {
        val input = _uiState.value.newModelInput.trim()
        if (input.isEmpty()) return

        viewModelScope.launch {
            addModel(Model(input))
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        newModelInput = "",
                        entry = _uiState.value.entry.copy(model = Model(input))
                    )
                }
        }
    }

    fun addNewLocation() {
        val input = _uiState.value.newLocationInput.trim()
        if (input.isEmpty()) return

        viewModelScope.launch {
            addLocation(Location(input))
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        newLocationInput = "",
                        entry = _uiState.value.entry.copy(location = Location(input))
                    )
                }
        }
    }

    // -------------------------
    // SAVE / DELETE
    // -------------------------

    fun saveEntry() {
        val state = _uiState.value
        if (!state.isValid) return

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                if (state.isEditMode) {
                    updateEntry(state.entry)
                } else {
                    createEntry(state.entry.copy(timestamp = System.currentTimeMillis()))
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveCompleted = true
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun deleteEntry() {
        if (!_uiState.value.isEditMode) return

        viewModelScope.launch {
            deleteEntry(_uiState.value.entry)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(saveCompleted = true)
                }
        }
    }

    fun consumeSaveCompleted() {
        _uiState.value = _uiState.value.copy(saveCompleted = false)
    }
}
