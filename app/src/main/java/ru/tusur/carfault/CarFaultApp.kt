package ru.tusur.carfault

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.androidx.compose.koinViewModel
import ru.tusur.core.ui.theme.CarFaultTheme
import ru.tusur.presentation.about.AboutScreen
import ru.tusur.presentation.entryedit.EditEntryDescriptionScreen
import ru.tusur.presentation.entrylist.EntryListScreen
import ru.tusur.presentation.entrynewmetadata.NewEntryMetadataScreen
import ru.tusur.presentation.entrysearch.EntrySearchScreen
import ru.tusur.presentation.entryview.RecordingViewScreen
import ru.tusur.presentation.mainscreen.MainScreen
import ru.tusur.presentation.settings.SettingsScreen
import ru.tusur.presentation.settings.SettingsViewModel

@Composable
fun CarFaultApp() {
    CarFaultTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "main") {

                composable("main") {
                    MainScreen(navController)
                }

                composable("recent_entries") {
                    EntryListScreen(navController, isSearchMode = false)
                }

                composable("search_entries") {
                    EntryListScreen(navController, isSearchMode = true)
                }

                composable("new_metadata") {
                    NewEntryMetadataScreen(navController)
                }

                composable("search") {
                    EntrySearchScreen(navController)
                }

                composable("settings") { backStackEntry ->
                    val viewModel: SettingsViewModel = koinViewModel(
                        viewModelStoreOwner = backStackEntry
                    )
                    SettingsScreen(
                        navController = navController,
                        viewModel = viewModel
                    )
                }

                composable("about") {
                    AboutScreen(navController)
                }

                composable(
                    route = "view_entry/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { entry ->
                    val id = entry.arguments!!.getLong("id")
                    RecordingViewScreen(navController, id)
                }

                composable(
                    route = "edit_entry_description/{entryId}",
                    arguments = listOf(
                        navArgument("entryId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    val entryId = backStackEntry.arguments!!.getLong("entryId")

                    EditEntryDescriptionScreen(
                        navController = navController,
                        entryId = entryId
                    )
                }
            }
        }
    }
}
