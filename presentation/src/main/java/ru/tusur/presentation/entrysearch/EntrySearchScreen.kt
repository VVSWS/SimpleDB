package ru.tusur.presentation.entrysearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.tusur.domain.model.SearchQuery
import ru.tusur.presentation.search.SharedSearchViewModel
import ru.tusur.domain.model.toFilter
import ru.tusur.presentation.R
import ru.tusur.presentation.common.component.EditableDropdownSelector

// ---------------------------------------------------------
// Экран расширенного поиска записей
// ---------------------------------------------------------
// Позволяет пользователю задать фильтры для поиска записей:
// - год выпуска
// - марка автомобиля
// - модель автомобиля
// - местоположение
// После нажатия кнопки поиска сохраняет фильтры в SharedSearchViewModel
// и переходит на экран списка результатов поиска
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrySearchScreen(
    navController: NavController,
    viewModel: EntrySearchViewModel = koinViewModel(),
    onSearch: (SearchQuery) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val sharedSearchViewModel: SharedSearchViewModel = koinViewModel()

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
                        stringResource(R.string.new_search_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    // Кнопка возврата на предыдущий экран
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { padding ->

        // ---------------------------------------------------------
        // Основной контент с прокруткой
        // ---------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // ---------------------------------------------------------
            // Выбор года (только для чтения, без добавления новых)
            // ---------------------------------------------------------
            EditableDropdownSelector(
                items = uiState.years,
                selectedItem = uiState.selectedYear,
                itemToString = { it.toString() },
                onItemSelected = { viewModel.onYearSelected(it) },
                onAddNewItem = {},           // Добавление новых значений отключено
                onDeleteItem = null,          // Удаление отключено
                errorMessage = null,
                showAddNewOption = false,     // Не показывать опцию "Добавить новое"
                placeholder = stringResource(R.string.hint_select_year)
            )

            Spacer(Modifier.height(12.dp))

            // ---------------------------------------------------------
            // Выбор марки (только для чтения)
            // ---------------------------------------------------------
            EditableDropdownSelector(
                items = uiState.brands,
                selectedItem = uiState.selectedBrand,
                itemToString = { it.toString() },
                onItemSelected = { viewModel.onBrandSelected(it) },
                onAddNewItem = {},
                onDeleteItem = null,
                errorMessage = null,
                showAddNewOption = false,
                placeholder = stringResource(R.string.hint_select_brand)
            )

            Spacer(Modifier.height(12.dp))

            // ---------------------------------------------------------
            // Выбор модели (только для чтения)
            // ---------------------------------------------------------
            EditableDropdownSelector(
                items = uiState.models,
                selectedItem = uiState.selectedModel,
                itemToString = { it.toString() },
                onItemSelected = { viewModel.onModelSelected(it) },
                onAddNewItem = {},
                onDeleteItem = null,
                errorMessage = null,
                showAddNewOption = false,
                placeholder = stringResource(R.string.hint_select_model)
            )

            Spacer(Modifier.height(12.dp))

            // ---------------------------------------------------------
            // Выбор местоположения (только для чтения)
            // ---------------------------------------------------------
            EditableDropdownSelector(
                items = uiState.locations,
                selectedItem = uiState.selectedLocation,
                itemToString = { it.toString() },
                onItemSelected = { viewModel.onLocationSelected(it) },
                onAddNewItem = {},
                onDeleteItem = null,
                errorMessage = null,
                showAddNewOption = false,
                placeholder = stringResource(R.string.hint_select_location)
            )

            Spacer(Modifier.height(24.dp))

            // ---------------------------------------------------------
            // Кнопка выполнения поиска
            // ---------------------------------------------------------
            Button(
                onClick = {
                    // Построение запроса из выбранных фильтров
                    val query = viewModel.buildSearchQuery()

                    // Сохранение фильтров в SharedSearchViewModel (общее состояние)
                    sharedSearchViewModel.updateFilter(query.toFilter())

                    // Навигация на экран списка результатов поиска
                    // launchSingleTop = true предотвращает создание дублирующихся экземпляров
                    navController.navigate("search_entries") { launchSingleTop = true }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.main_search_entries))
            }
        }
    }
}