package ru.tusur.presentation.entryview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.automirrored.filled.Help
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.tusur.presentation.R
import ru.tusur.presentation.common.ConfirmDeleteDialog
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri
import ru.tusur.core.ui.component.ImageThumbnail
import ru.tusur.presentation.entryview.components.ZoomableImage
import java.io.File
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingViewScreen(
    navController: NavController,
    entryId: Long
) {
    val viewModel: RecordingViewViewModel = koinViewModel(
        parameters = { parametersOf(entryId) }
    )

    val uiState by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Refresh when returning to this screen
    LaunchedEffect(entryId) {
        viewModel.refresh()
    }


    if (uiState.isDeleted) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.title_entry_details)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            uiState.entry?.id?.let { id ->
                                navController.navigate("edit_entry_description/$id")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }

                    // Help icon
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = stringResource(R.string.cd_help)
                        )
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Text(
                    text = "${stringResource(R.string.error_prefix)} ${uiState.error}",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> RecordingViewContent(
                uiState = uiState,
                modifier = Modifier.padding(padding)
            )
        }

        if (showDeleteDialog) {
            ConfirmDeleteDialog(
                itemName = uiState.entry?.title ?: stringResource(R.string.fallback_this_entry),
                onConfirm = {
                    viewModel.deleteEntry()
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }

        // AlertDialog
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = {
                    Text(stringResource(R.string.help_title_entryview))
                },
                text = {
                    Text(stringResource(R.string.help_information_entryview))
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text(stringResource(R.string.help_dialog_close_entryview))
                    }
                }
            )
        }
    }
}

@Composable
fun RecordingViewContent(
    uiState: RecordingViewUiState,
    modifier: Modifier = Modifier
) {
    val entry = uiState.entry ?: return
    var selectedImage by remember { mutableStateOf<String?>(null) }

    val formattedDate = remember(entry.timestamp) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(entry.timestamp))
    }

    val descriptionText = entry.description?.ifBlank { null }
        ?: stringResource(R.string.no_description_provided)

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        item {
            Text(
                text = entry.title.ifBlank { stringResource(R.string.untitled_entry) },
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${stringResource(id = R.string.show_date_sign)} $formattedDate")
                Text("${stringResource(id = R.string.show_year_sign)} ${entry.year?.value}")
                Text("${stringResource(id = R.string.show_brand_sign)} ${entry.brand?.name}")
                Text("${stringResource(id = R.string.show_model_sign)} ${entry.model?.name}")
                Text("${stringResource(id = R.string.show_location_sign)} ${entry.location?.name}")
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.show_description_sign),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(descriptionText)
            }
        }

        item {
            Text(
                text = stringResource(id = R.string.show_image_sign),
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (entry.imageUris.isEmpty()) {
            item {
                Text(stringResource(R.string.no_images))
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(entry.imageUris, key = { it }) { uri ->
                        val model = remember(uri) {
                            when {
                                uri.startsWith("content://") -> uri.toUri()
                                uri.startsWith("file://") -> uri.toUri()
                                uri.startsWith("/") -> File(uri)
                                else -> File(context.filesDir, uri)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clickable { selectedImage = uri }
                        ) {
                            ImageThumbnail(
                                uri = uri,
                                modifier = Modifier.size(140.dp),
                                onClick = { selectedImage = uri }
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedImage != null) {
        ZoomableImage(
            uri = selectedImage!!,
            onDismiss = { selectedImage = null }
        )
    }
}
