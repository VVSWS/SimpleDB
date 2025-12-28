package ru.tusur.presentation.entryview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.tusur.domain.model.EntryWithRecording
import ru.tusur.presentation.entryview.components.FullScreenImageViewer
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext


@Composable
fun RecordingViewScreen(
    navController: NavController,
    entryId: Long
) {
    val viewModel: RecordingViewViewModel = koinViewModel(
        parameters = { parametersOf(entryId) }
    )

    val uiState by viewModel.state.collectAsState()

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
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
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun RecordingViewContent(
    entry: EntryWithRecording,
    onBack: () -> Unit
) {
    // ⭐ Add this state
    var selectedImage by remember { mutableStateOf<String?>(null) }

    val formattedDate = remember(entry.timestamp) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(entry.timestamp))
    }

    val descriptionText = entry.description?.ifBlank { null } ?: "No description provided"
    val context = LocalContext.current

    Column(
        modifier = Modifier
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
        Text("Year: ${entry.year.value}")
        Text("Brand: ${entry.brand.name}")
        Text("Model: ${entry.model.name}")
        Text("Location: ${entry.location.name}")

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
                            .clickable {
                                println("IMAGE CLICKED: $uri")
                                selectedImage = uri   // ⭐ Set selected image
                            }
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

    // ⭐ Show full-screen viewer when an image is selected
    if (selectedImage != null) {
        FullScreenImageViewer(
            relativePath = selectedImage!!,
            onDismiss = { selectedImage = null }
        )
    }
}
