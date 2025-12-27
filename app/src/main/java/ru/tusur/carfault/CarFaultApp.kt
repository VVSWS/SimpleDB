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

            NavHost(navController = navController, startDestination = "main") {

                composable("main") { MainScreen(navController) }

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

                composable("settings") {
                    SettingsScreen(navController)
                }

                composable("about") {
                    AboutScreen(navController)
                }

                composable("view_entry/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")!!.toLong()
                    RecordingViewScreen(navController, id)
                }

                composable(
                    route = "edit_entry/{entryId}/{year}/{model}/{location}/{title}/description"
                ) { backStackEntry ->

                    val entryId = backStackEntry.arguments?.getString("entryId")?.toLongOrNull()
                    val year = backStackEntry.arguments?.getString("year")!!
                    val model = Uri.decode(backStackEntry.arguments?.getString("model")!!)
                    val location = Uri.decode(backStackEntry.arguments?.getString("location")!!)
                    val title = Uri.decode(backStackEntry.arguments?.getString("title")!!)

                    EditEntryDescriptionScreen(
                        navController = navController,
                        entryId = entryId,
                        year = year,
                        model = model,
                        location = location,
                        title = title
                    )
                }



            }
        }
    }
}
