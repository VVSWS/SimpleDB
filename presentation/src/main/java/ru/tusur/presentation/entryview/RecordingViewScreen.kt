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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.presentation.common.ConfirmDeleteDialog
import ru.tusur.presentation.entryview.components.FullScreenImageViewer
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
                title = { Text("Entry Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete entry"
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
                    text = "Error: ${uiState.error}",
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
            ConfirmDeleteDialog(
                itemName = uiState.entry?.title ?: "this entry",
                onConfirm = {
                    viewModel.deleteEntry()
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
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

    val descriptionText = entry.description?.ifBlank { null } ?: "No description provided"
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Title
        Text(
            text = entry.title.ifBlank { "Untitled Entry" },
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(12.dp))

        // Metadata
        Text("Date: $formattedDate")
        Text("Year: ${entry.year?.value}")
        Text("Brand: ${entry.brand?.name}")
        Text("Model: ${entry.model?.name}")
        Text("Location: ${entry.location?.name}")

        Spacer(Modifier.height(20.dp))

        // Description
        Text("Description:", style = MaterialTheme.typography.titleMedium)
        Text(descriptionText)

        Spacer(Modifier.height(20.dp))

        // Images
        Text("Images:", style = MaterialTheme.typography.titleMedium)

        if (entry.imageUris.isEmpty()) {
            Text("No images")
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
            Text("Back")
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
