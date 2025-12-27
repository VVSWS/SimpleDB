package ru.tusur.presentation.entryview

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.tusur.domain.model.EntryWithRecording
import java.text.SimpleDateFormat
import java.util.*

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
    // Format timestamp → readable date
    val formattedDate = remember(entry.date) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(entry.date))
    }

    // Safe description handling
    val descriptionText = entry.description?.ifBlank { null } ?: "No description provided"

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
        Text("Model: ${entry.model.name}")
        Text("Location: ${entry.location.name}")

        Spacer(Modifier.height(20.dp))

        // Description
        Text("Description:", style = MaterialTheme.typography.titleMedium)
        Text(descriptionText)

        Spacer(Modifier.height(20.dp))

        // Audio
        Text("Audio file:", style = MaterialTheme.typography.titleMedium)
        Text(entry.audioPath ?: "No audio")

        Spacer(Modifier.height(32.dp))

        // Back button
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
