package ru.tusur.presentation.entrynewmetadata

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import ru.tusur.presentation.common.component.EditableDropdownSelector
import ru.tusur.presentation.common.component.CompactTextField
import ru.tusur.presentation.R
import ru.tusur.domain.model.FaultEntry
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

// ---------------------------------------------------------
// Экран ввода метаданных для новой записи
// ---------------------------------------------------------
// Позволяет пользователю выбрать или добавить:
// - год выпуска
// - марку автомобиля
// - модель автомобиля
// - местоположение неисправности
// - заголовок записи
// После заполнения создаёт черновик записи и переходит к экрану редактирования описания
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryMetadataScreen(navController: NavController) {
    val viewModel: NewEntryMetadataViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }

    // ---------------------------------------------------------
    // Структура экрана: TopBar + контент
    // ---------------------------------------------------------
    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.new_entry_metadata_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    // Кнопка возврата
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    // Кнопка помощи (справка)
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = stringResource(R.string.cd_help)
                        )
                    }
                }
            )
        }
    ) { padding ->

        // ---------------------------------------------------------
        // Основной контент с прокруткой и учётом клавиатуры
        // ---------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()                    // Учёт открытой клавиатуры
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // ---------------------------------------------------------
            // Выбор/добавление года выпуска
            // ---------------------------------------------------------
            EditableDropdownSelector(
                items = uiState.years,
                selectedItem = uiState.selectedYear,
                itemToString = { it.value.toString() },
                onItemSelected = viewModel::onYearSelected,
                onAddNewItem = {
                    viewModel.onNewYearInputChanged(it)
                    viewModel.addNewYear()
                },
                onDeleteItem = viewModel::deleteYear,
                errorMessage = uiState.yearErrorMessage,
                showAddNewOption = true,
                placeholder = stringResource(R.string.hint_select_or_add_year)
            )

            Spacer(Modifier.height(12.dp))

            // ---------------------------------------------------------
            // Выбор/добавление марки автомобиля
            // ---------------------------------------------------------
            EditableDropdownSelector(
                items = uiState.brands,
                selectedItem = uiState.selectedBrand,
                itemToString = { it.name },
                onItemSelected = viewModel::onBrandSelected,
                onAddNewItem = {
                    viewModel.onNewBrandInputChanged(it)
                    viewModel.addNewBrand()
                },
                onDeleteItem = viewModel::deleteBrand,
                showAddNewOption = true,
                placeholder = stringResource(R.string.hint_select_or_add_brand)
            )

            Spacer(Modifier.height(12.dp))

            // ---------------------------------------------------------
            // Выбор/добавление модели автомобиля
            // ---------------------------------------------------------
            EditableDropdownSelector(
                items = uiState.models,
                selectedItem = uiState.selectedModel,
                itemToString = { it.name },
                onItemSelected = viewModel::onModelSelected,
                onAddNewItem = {
                    viewModel.onNewModelInputChanged(it)
                    viewModel.addNewModel()
                },
                onDeleteItem = viewModel::deleteModel,
                showAddNewOption = true,
                placeholder = stringResource(R.string.hint_select_or_add_model)
            )

            Spacer(Modifier.height(12.dp))

            // ---------------------------------------------------------
            // Выбор/добавление местоположения
            // ---------------------------------------------------------
            EditableDropdownSelector(
                items = uiState.locations,
                selectedItem = uiState.selectedLocation,
                itemToString = { it.name },
                onItemSelected = viewModel::onLocationSelected,
                onAddNewItem = {
                    viewModel.onNewLocationInputChanged(it)
                    viewModel.addNewLocation()
                },
                onDeleteItem = viewModel::deleteLocation,
                showAddNewOption = true,
                placeholder = stringResource(R.string.hint_select_or_add_location)
            )

            Spacer(Modifier.height(16.dp))

            // ---------------------------------------------------------
            // Поле ввода заголовка
            // ---------------------------------------------------------
            CompactTextField(
                label = stringResource(R.string.label_title_brief),
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                isError = uiState.title.isBlank(),
                errorMessage = if (uiState.title.isBlank())
                    stringResource(R.string.error_title_empty)
                else null
            )

            Spacer(Modifier.height(24.dp))

            // ---------------------------------------------------------
            // Кнопка "Продолжить" (создание черновика и переход)
            // ---------------------------------------------------------
            Button(
                onClick = {
                    // Создание черновика записи из состояния UI
                    val entry = FaultEntry(
                        year = uiState.selectedYear,      // Год (доменная модель)
                        brand = uiState.selectedBrand,    // Марка (доменная модель)
                        model = uiState.selectedModel,    // Модель (доменная модель)
                        location = uiState.selectedLocation,  // Локация (доменная модель)
                        title = uiState.title,
                        timestamp = System.currentTimeMillis()
                    )
                    // Создание записи и навигация на экран редактирования описания
                    viewModel.createEntry { newId ->
                        navController.navigate("edit_entry_description/$newId")
                    }
                },
                enabled = uiState.isContinueEnabled,  // Активна только при валидных данных
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_continue))
            }
        }
    }

    // ---------------------------------------------------------
    // Диалог справки (помощь по заполнению формы)
    // ---------------------------------------------------------
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(stringResource(R.string.help_title_newentry))
            },
            text = {
                Text(stringResource(R.string.help_information_newentry))
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(stringResource(R.string.help_dialog_close_newentry))
                }
            }
        )
    }
}