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
        uiState.isLoading -> Text("Loading...")
        uiState.error != null -> Text("Error: ${uiState.error}")

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
    val dateFormatted = remember(entry.date) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(entry.date))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(entry.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        Text("Date: $dateFormatted")
        Text("Year: ${entry.year.value}")
        Text("Model: ${entry.model.name}")
        Text("Location: ${entry.location.name}")

        Spacer(Modifier.height(16.dp))

        Text("Description:")
        Text(entry.description ?: "No description")

        Spacer(Modifier.height(16.dp))

        Text("Audio file:")
        Text(entry.audioPath ?: "No audio")

        Spacer(Modifier.height(24.dp))

        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
