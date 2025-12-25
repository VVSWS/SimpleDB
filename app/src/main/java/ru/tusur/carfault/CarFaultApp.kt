package ru.tusur.carfault

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.tusur.core.ui.theme.CarFaultTheme
import ru.tusur.presentation.about.AboutScreen
import ru.tusur.presentation.entryedit.EditEntryDescriptionScreen
import ru.tusur.presentation.entryedit.EditEntryMetadataScreen
import ru.tusur.presentation.entrylist.EntryListScreen
import ru.tusur.presentation.entrynewmetadata.NewEntryMetadataScreen
import ru.tusur.presentation.entrysearch.EntrySearchScreen
import ru.tusur.presentation.mainscreen.MainScreen
import ru.tusur.presentation.settings.SettingsScreen

@Composable
fun CarFaultApp() {
    CarFaultTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "main") {
                composable("main") { MainScreen(navController) }
                composable("recent_entries") { EntryListScreen(navController, filter = "recent") }
                composable("search_entries") { EntryListScreen(navController, filter = "search") }
                composable("new_metadata") { NewEntryMetadataScreen(navController) }
                composable("edit_entry/{entryId?}") { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getString("entryId")?.toLongOrNull()
                    navController.navigate("edit_entry/${entryId}/metadata")
                }

                composable("search") { EntrySearchScreen(navController) }
                composable("settings") { SettingsScreen(navController) }
                composable("about") { AboutScreen(navController) }
                composable("edit_entry/{id}/metadata") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                    EditEntryMetadataScreen(navController, id)
                }

                composable("edit_entry/{id}/description") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                    EditEntryDescriptionScreen(navController, id)
                }

            }
        }
    }
}