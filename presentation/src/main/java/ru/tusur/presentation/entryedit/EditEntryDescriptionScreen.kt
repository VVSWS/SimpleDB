package ru.tusur.presentation.entryedit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import org.koin.compose.koinInject
import ru.tusur.domain.model.*
import ru.tusur.presentation.R
import ru.tusur.presentation.common.DescriptionError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryDescriptionScreen(
    navController: NavController,
    entryId: Long,
    year: Int,
    brand: String,
    model: String,
    location: String,
    title: String
) {
    val viewModel: EditEntryDescriptionViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onImagesSelected(navController.context, uris)
        }
    }

    // Initialize entry
    LaunchedEffect(entryId) {
        if (entryId == 0L) {
            val initial = FaultEntry(
                year = Year(year),
                brand = Brand(brand),
                model = Model(model, Brand(brand), Year(year)),
                location = Location(location),
                title = title
            )

            viewModel.initializeNewEntry(initial)
        } else {
            viewModel.loadEntry(entryId)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.edit_description_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        imagePickerLauncher.launch("image/*")
                    }) {
                        Icon(
                            Icons.Filled.AddAPhoto,
                            contentDescription = stringResource(R.string.cd_add_image)
                        )
                    }
                }
            )

        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // DESCRIPTION FIELD
            OutlinedTextField(
                value = uiState.entry.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text(stringResource(R.string.label_description)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.descriptionError != null
            )


            uiState.descriptionError?.let { error ->
                val message = when (error) {
                    DescriptionError.Empty -> stringResource(R.string.error_description_empty)
                }

                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }


            // IMAGE PREVIEW
            if (uiState.entry.imageUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.entry.imageUris) { path ->
                        val file = navController.context.filesDir.resolve(path)
                        val fileUri = Uri.fromFile(file)

                        Image(
                            painter = rememberAsyncImagePainter(fileUri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .padding(4.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                }


            }

            Spacer(Modifier.height(24.dp))

            // SAVE BUTTON
            Button(
                onClick = { viewModel.saveEntry() },
                enabled = uiState.isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_save))
            }


            // DELETE BUTTON (only in edit mode)
            if (uiState.isEditMode) {
                Button(
                    onClick = { viewModel.deleteEntry() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.button_delete))
                }

            }

            // NAVIGATE BACK AFTER SAVE
            if (uiState.saveCompleted) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                    viewModel.consumeSaveCompleted()
                }
            }
        }
    }
}
