package ru.tusur.carfault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinApplication
import ru.tusur.presentation.about.AboutScreen
import ru.tusur.presentation.entryedit.EditEntryScreen
import ru.tusur.presentation.entrylist.EntryListScreen
import ru.tusur.presentation.entrynewmetadata.NewEntryMetadataScreen
import ru.tusur.presentation.entrysearch.EntrySearchScreen
import ru.tusur.presentation.mainscreen.MainScreen
import ru.tusur.presentation.settings.SettingsScreen
import ru.tusur.carfault.ui.theme.CarFaultTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CarFaultTheme {

                val navController = rememberNavController()

                BackHandler {
                    if (!navController.popBackStack()) {
                        finish()
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "main"
                    ) {
                        composable("main") {
                            MainScreen(navController)
                        }
                        composable("recent_entries") {
                            EntryListScreen(navController, filter = "recent")
                        }
                        composable("search_entries") {
                            EntryListScreen(navController, filter = "search")
                        }
                        composable("new_metadata") {
                            NewEntryMetadataScreen(navController)
                        }
                        composable("edit_entry") {
                            EditEntryScreen(navController, entryId = null)
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
                    }
                }
            }
        }
    }
}