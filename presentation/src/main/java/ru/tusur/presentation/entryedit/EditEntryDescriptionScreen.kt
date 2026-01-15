package ru.tusur.presentation.entryedit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ru.tusur.core.ui.component.ImageThumbnail
import ru.tusur.presentation.R
import ru.tusur.presentation.common.DescriptionError
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryDescriptionScreen(
    navController: NavController,
    entryId: Long
) {
    // Inject ViewModel with ID parameter
    val viewModel: EditEntryDescriptionViewModel =
        koinInject(parameters = { parametersOf(entryId) })

    val uiState by viewModel.uiState.collectAsState()
    val context = navController.context


    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onImagesSelected(navController.context, uris)
        }
    }

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
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
                .imePadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // DESCRIPTION FIELD
            OutlinedTextField(
                value = entry.description,
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
                    items(
                        items = entry.imageUris,
                        key = { uri -> uri }   // IMPORTANT: stable key
                    ) { uri ->

                        var showDeleteDialog by remember { mutableStateOf(false) }

                        Box {
                            ImageThumbnail(
                                uri = uri,
                                modifier = Modifier.size(120.dp),
                                onClick = { /* optional */ }
                            )

                            // Delete button with red background
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.error,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.cd_delete_image),
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }

                            // Confirmation dialog
                            if (showDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteDialog = false },
                                    title = { Text(stringResource(R.string.cd_delete_image)) },
                                    text = { Text(stringResource(R.string.confirm_delete_message)) },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                showDeleteDialog = false
                                                viewModel.removeImage(context, uri)
                                            }
                                        ) {
                                            Text(stringResource(R.string.dialog_delete_confirm))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteDialog = false }) {
                                            Text(stringResource(R.string.dialog_delete_cancel))
                                        }
                                    }
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
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
            ) {
                Text(stringResource(R.string.button_cancel))
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
