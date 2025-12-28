package ru.tusur.presentation.entryedit

import android.net.Uri
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.Year
import ru.tusur.domain.model.Model
import ru.tusur.domain.model.Location
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import ru.tusur.presentation.entryview.components.FullScreenImageViewer
import androidx.core.net.toUri
import java.io.File
import androidx.compose.ui.platform.LocalContext



@Composable
fun EditEntryDescriptionScreen(
    navController: NavController,
    entryId: Long?,
    year: Int,
    brand: String,
    model: String,
    location: String,
    title: String
) {
    val viewModel: EditEntryViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedImage by remember { mutableStateOf<String?>(null) }



    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        viewModel.onImagesSelected(context, uris)
    }

    // Apply metadata ONLY for new entries
    LaunchedEffect(Unit) {
        if (entryId == null || entryId == 0L) {
            viewModel.onYearChanged(Year(year))
            viewModel.onBrandChanged(Brand(brand))
            viewModel.onModelChanged(Model(model))
            viewModel.onLocationChanged(Location(location))
            viewModel.onTitleChanged(title)
        }
    }

    // Load entry for editing
    LaunchedEffect(entryId) {
        if (entryId != null && entryId > 0L) {
            viewModel.loadEntry(entryId)
        }
    }

    // Handle save completion
    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {
            viewModel.consumeSaveCompleted()
            navController.popBackStack()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    println("ROOT TAP")
                }
            }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Description",
                style = MaterialTheme.typography.headlineSmall
            )

            // DESCRIPTION FIELD
            OutlinedTextField(
                value = uiState.entry.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Description (≤500)") },
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            // IMAGE PREVIEW
            if (uiState.entry.imageUris.isNotEmpty()) {
                Text(
                    text = "Attached Images:",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.entry.imageUris) { uri ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
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

            // ADD IMAGES BUTTON
            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Images")
            }

            Spacer(Modifier.height(24.dp))

            // BACK + SAVE BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = viewModel::saveEntry,
                    enabled = uiState.isValid && !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }

            // DELETE BUTTON (only in edit mode)
            if (uiState.isEditMode) {
                TextButton(
                    onClick = viewModel::deleteEntry,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Delete Entry", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        if (selectedImage != null) {
            FullScreenImageViewer(
                relativePath = selectedImage!!,
                onDismiss = { selectedImage = null }
            )
        }

    }
}
