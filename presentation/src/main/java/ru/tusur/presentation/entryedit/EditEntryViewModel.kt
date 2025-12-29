package ru.tusur.presentation.entryedit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.tusur.core.files.savePickedImage
import ru.tusur.domain.model.*
import ru.tusur.domain.usecase.entry.*
import ru.tusur.domain.usecase.reference.*

class EditEntryViewModel(
    private val getEntryById: GetEntryByIdUseCase,
    private val createEntry: CreateEntryUseCase,
    private val updateEntry: UpdateEntryUseCase,
    private val deleteEntry: DeleteEntryUseCase,

    private val getYears: GetYearsUseCase,
    private val getBrands: GetBrandsUseCase,
    private val getModelsForBrandAndYear: GetModelsForBrandAndYearUseCase,
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

    // Reactive brand/year selections
    private val selectedBrand = MutableStateFlow<Brand?>(null)
    private val selectedYear = MutableStateFlow<Year?>(null)

    // Reactive model list based on Brand + Year
    private val modelsFlow: Flow<List<Model>> =
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
            modelsFlow,
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
                    val e = entry ?: FaultEntry()

                    selectedBrand.value = e.brand
                    selectedYear.value = e.year

                    _uiState.value = _uiState.value.copy(
                        entry = e,
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
        selectedYear.value = year

        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(
                year = year,
                model = null
            )
        )
    }

    fun onModelNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(
                model = _uiState.value.entry.model?.copy(name = name)
            )
        )
    }


    fun onBrandChanged(brand: Brand) {
        selectedBrand.value = brand

        _uiState.value = _uiState.value.copy(
            entry = _uiState.value.entry.copy(
                brand = brand,
                model = null
            )
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
    // IMAGE HANDLING
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

        val year = Year(input.toInt())

        viewModelScope.launch {
            addYear(year).onSuccess {
                _uiState.value = _uiState.value.copy(
                    newYearInput = "",
                    entry = _uiState.value.entry.copy(year = year)
                )
                selectedYear.value = year
            }
        }
    }

    fun addNewBrand() {
        val input = _uiState.value.newBrandInput.trim()
        if (input.isEmpty()) return

        val brand = Brand(input)

        viewModelScope.launch {
            addBrand(brand).onSuccess {
                _uiState.value = _uiState.value.copy(
                    newBrandInput = "",
                    entry = _uiState.value.entry.copy(brand = brand)
                )
                selectedBrand.value = brand
            }
        }
    }

    fun addNewModel() {
        val input = _uiState.value.newModelInput.trim()
        val brand = _uiState.value.entry.brand ?: return
        val year = _uiState.value.entry.year ?: return
        if (input.isEmpty()) return

        val model = Model(input, brand, year)

        viewModelScope.launch {
            addModel(model).onSuccess {
                _uiState.value = _uiState.value.copy(
                    newModelInput = "",
                    entry = _uiState.value.entry.copy(model = model)
                )
            }
        }
    }

    fun addNewLocation() {
        val input = _uiState.value.newLocationInput.trim()
        if (input.isEmpty()) return

        val location = Location(input)

        viewModelScope.launch {
            addLocation(location).onSuccess {
                _uiState.value = _uiState.value.copy(
                    newLocationInput = "",
                    entry = _uiState.value.entry.copy(location = location)
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
