package ru.tusur.presentation.entrylist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.tusur.presentation.search.SharedSearchViewModel
import ru.tusur.domain.model.FaultEntry
import ru.tusur.presentation.R
import ru.tusur.presentation.common.ConfirmDeleteDialog
import ru.tusur.presentation.common.EntryListError
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------
// Экран списка записей о неисправностях
// ---------------------------------------------------------
// Поддерживает два режима: последние записи (recent) и результаты поиска (search)
// Отображает список карточек с заголовком, метаданными и кнопкой удаления
// Включает кастомную вертикальную полосу прокрутки
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryListScreen(
    navController: NavController,
    isSearchMode: Boolean = false,                      // Режим: поиск или последние записи
    viewModel: EntryListViewModel = koinViewModel(),
    sharedSearchViewModel: SharedSearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filter by sharedSearchViewModel.filter.collectAsState()
    val listState = rememberLazyListState()

    // ---------------------------------------------------------
    // Загрузка данных в зависимости от режима
    // ---------------------------------------------------------
    LaunchedEffect(isSearchMode, filter) {
        if (isSearchMode) {
            viewModel.searchEntries(filter)   // Режим поиска
        } else {
            viewModel.loadRecentEntries()      // Режим последних записей
        }
    }

    // Заголовок экрана
    val title = if (isSearchMode)
        stringResource(R.string.entry_list_search_results)
    else
        stringResource(R.string.entry_list_recent)

    // Состояние для диалога удаления
    var entryToDelete by remember { mutableStateOf<FaultEntry?>(null) }

    // ---------------------------------------------------------
    // Диалог подтверждения удаления
    // ---------------------------------------------------------
    if (entryToDelete != null) {
        ConfirmDeleteDialog(
            itemName = entryToDelete!!.title.ifBlank {
                stringResource(R.string.entry_list_fallback_entry)
            },
            onConfirm = {
                viewModel.deleteEntry(entryToDelete!!)
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }

    // ---------------------------------------------------------
    // Структура экрана: TopBar + контент
    // ---------------------------------------------------------
    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        contentWindowInsets = WindowInsets(0, 0, 0, 60),  // Кастомные отступы
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 0.dp,
                    bottom = padding.calculateBottomPadding(),
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr)
                )
                .padding(horizontal = 4.dp)
        ) {

            // ---------------------------------------------------------
            // Индикатор загрузки
            // ---------------------------------------------------------
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            // ---------------------------------------------------------
            // Сообщение об ошибке
            // ---------------------------------------------------------
            uiState.error?.let { error ->
                val message = when (error) {
                    EntryListError.LoadFailed -> stringResource(R.string.error_entry_list_load)
                    EntryListError.SearchFailed -> stringResource(R.string.error_entry_list_search)
                }

                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // ---------------------------------------------------------
            // Список записей с кастомной полосой прокрутки
            // ---------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // ---------------------------------------------------------
                // Основной LazyColumn с записями
                // ---------------------------------------------------------
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.entries) { entry ->
                        EntryListItem(
                            entry = entry,
                            onClick = { navController.navigate("view_entry/${entry.id}") },
                            onDelete = { entryToDelete = entry }
                        )
                    }
                }

                // Отступ между списком и скроллбаром
                Spacer(modifier = Modifier.width(6.dp))

                // ---------------------------------------------------------
                // Кастомная вертикальная полоса прокрутки
                // ---------------------------------------------------------
                LazyColumnScrollbar(
                    state = listState,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(6.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// Компонент отдельной записи в списке (карточка)
// ---------------------------------------------------------
@Composable
fun EntryListItem(
    entry: FaultEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ---------------------------------------------------------
            // Левая часть: заголовок + метаданные
            // ---------------------------------------------------------
            Column(modifier = Modifier.weight(1f)) {
                // Сбор метаданных в строку с разделителем " • "
                val meta = listOfNotNull(
                    entry.year?.value?.toString(),
                    entry.brand?.name,
                    entry.model?.name,
                    entry.location?.name
                ).joinToString(" • ")

                // Заголовок записи (или плейсхолдер)
                Text(
                    text = entry.title.ifBlank { stringResource(R.string.entry_list_no_title) },
                    style = MaterialTheme.typography.titleMedium
                )

                // Метаданные (если не пустые)
                if (meta.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ---------------------------------------------------------
            // Правая часть: иконка удаления
            // ---------------------------------------------------------
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.entry_list_delete_cd)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// Кастомный вертикальный скроллбар для LazyColumn
// ---------------------------------------------------------
@Composable
private fun LazyColumnScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp,                    // Толщина полосы прокрутки
    color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)  // Цвет ползунка
) {
    val layoutInfo = state.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    if (totalItems == 0) return

    val firstVisible = state.firstVisibleItemIndex
    val visibleItems = layoutInfo.visibleItemsInfo.size

    // Если видимы все элементы - скроллбар не нужен
    if (totalItems == 0 || visibleItems >= totalItems) return

    // Расчёт позиции ползунка на основе индекса первого видимого элемента
    val fraction = remember(firstVisible, totalItems) {
        firstVisible.toFloat() / (totalItems - visibleItems).coerceAtLeast(1)
    }

    Box(
        modifier = modifier
            .width(thickness)
            .fillMaxHeight()
            .drawBehind {
                // Фон трека (дорожки прокрутки)
                drawRoundRect(
                    color = color.copy(alpha = 0.15f),
                    cornerRadius = CornerRadius(size.width / 2)
                )

                // Ползунок
                val thumbHeight = size.height * (visibleItems.toFloat() / totalItems)
                val thumbTop = (size.height - thumbHeight) * fraction

                drawRoundRect(
                    color = color,
                    topLeft = Offset(0f, thumbTop),
                    size = Size(size.width, thumbHeight),
                    cornerRadius = CornerRadius(size.width / 2)
                )
            }
    )
}