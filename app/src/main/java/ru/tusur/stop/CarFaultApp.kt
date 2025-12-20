package ru.tusur.stop

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import ru.tusur.stop.core.ui.theme.CarFaultTheme
import ru.tusur.stop.presentation.about.AboutScreen
import ru.tusur.stop.presentation.entryedit.EditEntryScreen
import ru.tusur.stop.presentation.entrylist.EntryListScreen
import ru.tusur.stop.presentation.entrynewmetadata.NewEntryMetadataScreen
import ru.tusur.stop.presentation.entrysearch.EntrySearchScreen
import ru.tusur.stop.presentation.mainscreen.MainScreen
import ru.tusur.stop.presentation.settings.SettingsScreen

@Composable
fun CarFaultApp() {
    KoinApplication(application = {
        val context = LocalContext.current.applicationContext as Application
        startKoin {
            androidContext(context)
            modules(appModule)
        }
    }) {
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
                        EditEntryScreen(navController, entryId)
                    }
                    composable("search") { EntrySearchScreen(navController) }
                    composable("settings") { SettingsScreen(navController) }
                    composable("about") { AboutScreen(navController) }
                }
            }
        }
    }
}