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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import org.koin.compose.koinInject
import ru.tusur.presentation.R
import ru.tusur.presentation.common.DescriptionError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryDescriptionScreen(
    navController: NavController,
    entryId: Long
) {
    val viewModel: EditEntryDescriptionViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    // Load entry once
    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onImagesSelected(navController.context, uris)
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

        // If entry is still loading or null, show nothing yet
        val entry = uiState.entry
        if (entry == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

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
                value = entry.description ?: "",
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
            if (entry.imageUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(entry.imageUris) { path ->
                        Box {
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

                            IconButton(
                                onClick = { viewModel.removeImage(path) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.cd_delete_image),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
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

            // CANCEL BUTTON
            if (uiState.isEditMode) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.button_cancel))
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
