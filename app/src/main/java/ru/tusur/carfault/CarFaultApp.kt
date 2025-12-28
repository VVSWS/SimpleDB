package ru.tusur.carfault

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
import ru.tusur.core.ui.theme.CarFaultTheme
import ru.tusur.presentation.about.AboutScreen
import ru.tusur.presentation.entryedit.EditEntryDescriptionScreen
import ru.tusur.presentation.entrylist.EntryListScreen
import ru.tusur.presentation.entrynewmetadata.NewEntryMetadataScreen
import ru.tusur.presentation.entrysearch.EntrySearchScreen
import ru.tusur.presentation.mainscreen.MainScreen
import ru.tusur.presentation.settings.SettingsScreen
import ru.tusur.presentation.entryview.RecordingViewScreen
import android.net.Uri

@Composable
fun CarFaultApp() {
    CarFaultTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "main"
            ) {

                // Main screen
                composable("main") {
                    MainScreen(navController)
                }

                // Recent entries
                composable("recent_entries") {
                    EntryListScreen(navController, isSearchMode = false)
                }

                // Search results
                composable("search_entries") {
                    EntryListScreen(navController, isSearchMode = true)
                }

                // New entry metadata
                composable("new_metadata") {
                    NewEntryMetadataScreen(navController)
                }

                // Search screen
                composable("search") {
                    EntrySearchScreen(navController)
                }

                // Settings
                composable("settings") {
                    SettingsScreen(navController)
                }

                // About
                composable("about") {
                    AboutScreen(navController)
                }

                // View entry
                composable(
                    route = "view_entry/{id}",
                    arguments = listOf(
                        navArgument("id") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    val id = backStackEntry.arguments!!.getLong("id")
                    RecordingViewScreen(navController, id)
                }

                // Edit entry description
                composable(
                    route = "edit_entry/{entryId}/{year}/{brand}/{model}/{location}/{title}/description",
                    arguments = listOf(
                        navArgument("entryId") { type = NavType.LongType },
                        navArgument("year") {
                            type = NavType.IntType
                            nullable = false
                        },
                        navArgument("brand") { type = NavType.StringType },
                        navArgument("model") { type = NavType.StringType },
                        navArgument("location") { type = NavType.StringType },
                        navArgument("title") { type = NavType.StringType }
                    )
                ) { backStackEntry ->

                    val entryId = backStackEntry.arguments!!.getLong("entryId")
                    val year = backStackEntry.arguments!!.getInt("year")
                    val brand = Uri.decode(backStackEntry.arguments!!.getString("brand")!!)
                    val model = Uri.decode(backStackEntry.arguments!!.getString("model")!!)
                    val location = Uri.decode(backStackEntry.arguments!!.getString("location")!!)
                    val title = Uri.decode(backStackEntry.arguments!!.getString("title")!!)

                    EditEntryDescriptionScreen(
                        navController = navController,
                        entryId = entryId,
                        year = year,
                        brand = brand,
                        model = model,
                        location = location,
                        title = title
                    )
                }
            }
        }
    }
}
