package ru.tusur.presentation.entryview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.presentation.R
import ru.tusur.presentation.common.ConfirmDeleteDialog
import ru.tusur.presentation.entryview.components.FullScreenImageViewer
import ru.tusur.presentation.localization.LocalAppLanguage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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
    val appLanguage = LocalAppLanguage.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Navigate back after deletion
    if (uiState.isDeleted) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }

    Scaffold(
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
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete_entry)
                        )
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

            else -> uiState.entry?.let { entry ->
                RecordingViewContent(
                    entry = entry,
                    onBack = { navController.popBackStack() },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        if (showDeleteDialog) {
            key(appLanguage.locale) {
                ConfirmDeleteDialog(
                    itemName = uiState.entry?.title ?: stringResource(R.string.fallback_this_entry),
                    onConfirm = {
                        viewModel.deleteEntry()
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }
        }
    }
}

@Composable
fun RecordingViewContent(
    entry: EntryWithRecording,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedImage by remember { mutableStateOf<String?>(null) }

    val formattedDate = remember(entry.timestamp) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(entry.timestamp))
    }

    val descriptionText = entry.description?.ifBlank { null }
        ?: stringResource(R.string.no_description_provided)
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Title
        Text(
            text = entry.title.ifBlank { stringResource(R.string.untitled_entry) },
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(12.dp))

        // Metadata
        Text("${stringResource(id = R.string.show_date_sign)} $formattedDate")
        Text("${stringResource(id = R.string.show_year_sign)} ${entry.year?.value}")
        Text("${stringResource(id = R.string.show_brand_sign)} ${entry.brand?.name}")
        Text("${stringResource(id = R.string.show_model_sign)} ${entry.model?.name}")
        Text("${stringResource(id = R.string.show_location_sign)} ${entry.location?.name}")

        Spacer(Modifier.height(20.dp))

        // Description
        Text(text = stringResource(id = R.string.show_description_sign), style = MaterialTheme.typography.titleMedium)
        Text(descriptionText)

        Spacer(Modifier.height(20.dp))

        // Images
        Text(text = stringResource(id = R.string.show_image_sign), style = MaterialTheme.typography.titleMedium)

        if (entry.imageUris.isEmpty()) {
            Text(stringResource(R.string.no_images))
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(entry.imageUris) { uri ->
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .padding(4.dp)
                            .clickable { selectedImage = uri }
                    ) {
                        AsyncImage(
                            model = File(context.filesDir, uri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Back button
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.button_back))
        }
    }

    // Full-screen image viewer
    if (selectedImage != null) {
        FullScreenImageViewer(
            relativePath = selectedImage!!,
            onDismiss = { selectedImage = null }
        )
    }
}
