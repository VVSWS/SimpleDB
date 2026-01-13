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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryListScreen(
    navController: NavController,
    isSearchMode: Boolean = false,
    viewModel: EntryListViewModel = koinViewModel(),
    sharedSearchViewModel: SharedSearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filter by sharedSearchViewModel.filter.collectAsState()
    val listState = rememberLazyListState()

    // Load data depending on mode
    LaunchedEffect(isSearchMode, filter) {
        if (isSearchMode) {
            viewModel.searchEntries(filter)
        } else {
            viewModel.loadRecentEntries()
        }
    }

    val title = if (isSearchMode)
        stringResource(R.string.entry_list_search_results)
    else
        stringResource(R.string.entry_list_recent)

    var entryToDelete by remember { mutableStateOf<FaultEntry?>(null) }

    // Delete confirmation dialog
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

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
            contentWindowInsets = WindowInsets(0, 0, 0, 60),
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

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            // Error message
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

            // Entries list


            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {

                // LIST
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

                // SPACE between list and scrollbar
                Spacer(modifier = Modifier.width(6.dp))

                // SCROLLBAR
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

            // Left side: title + metadata
            Column(modifier = Modifier.weight(1f)) {

                val meta = listOfNotNull(
                    entry.year?.value?.toString(),
                    entry.brand?.name,
                    entry.model?.name,
                    entry.location?.name
                ).joinToString(" • ")

                Text(
                    text = entry.title ?: stringResource(R.string.entry_list_no_title),
                    style = MaterialTheme.typography.titleMedium
                )

                if (meta.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right side: delete icon
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.entry_list_delete_cd)
                )
            }
        }
    }
}

@Composable
private fun LazyColumnScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
) {
    val layoutInfo = state.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    if (totalItems == 0) return

    val firstVisible = state.firstVisibleItemIndex
    val visibleItems = layoutInfo.visibleItemsInfo.size

    if (totalItems == 0 || visibleItems >= totalItems) return

    val fraction = remember(firstVisible, totalItems) {
        firstVisible.toFloat() / (totalItems - visibleItems).coerceAtLeast(1)
    }

    Box(
        modifier = modifier
            .width(thickness)
            .fillMaxHeight()
            .drawBehind {
                // Track
                drawRoundRect(
                    color = color.copy(alpha = 0.15f),
                    cornerRadius = CornerRadius(size.width / 2)
                )

                // Thumb
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
