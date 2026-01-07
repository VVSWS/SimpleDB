package ru.tusur.carfault

import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import ru.tusur.core.ui.theme.CarFaultTheme
import ru.tusur.presentation.about.AboutScreen
import ru.tusur.presentation.entryedit.EditEntryDescriptionScreen
import ru.tusur.presentation.entrylist.EntryListScreen
import ru.tusur.presentation.entrynewmetadata.NewEntryMetadataScreen
import ru.tusur.presentation.entrysearch.EntrySearchScreen
import ru.tusur.presentation.entryview.RecordingViewScreen
import ru.tusur.presentation.localization.AppLanguageState
import ru.tusur.presentation.localization.LocalAppLanguage
import ru.tusur.presentation.mainscreen.MainScreen
import ru.tusur.presentation.settings.SettingsScreen
import ru.tusur.presentation.settings.SettingsViewModel
import java.util.Locale

@Composable
fun CarFaultApp() {
    val dataStore: DataStore<Preferences> = koinInject()
    val keyLanguage = remember { stringPreferencesKey("language") }

    val langCode by dataStore.data
        .map { prefs -> prefs[keyLanguage] ?: "en" }
        .collectAsState(initial = "en")

    val appLanguageState = remember {
        AppLanguageState(Locale.forLanguageTag(langCode))
    }

    LaunchedEffect(langCode) {
        val locale = Locale.forLanguageTag(langCode)
        appLanguageState.updateLocale(locale)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(langCode)
        )
    }

    CompositionLocalProvider(LocalAppLanguage provides appLanguageState) {
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
                        route = "edit_entry/{entryId}/{year}/{brand}/{model}/{location}/{title}/description",
                        arguments = listOf(
                            navArgument("entryId") { type = NavType.LongType },
                            navArgument("year") { type = NavType.IntType },
                            navArgument("brand") { type = NavType.StringType },
                            navArgument("model") { type = NavType.StringType },
                            navArgument("location") { type = NavType.StringType },
                            navArgument("title") { type = NavType.StringType }
                        )
                    ) { entry ->
                        val entryId = entry.arguments!!.getLong("entryId")
                        val year = entry.arguments!!.getInt("year")
                        val brand = Uri.decode(entry.arguments!!.getString("brand")!!)
                        val model = Uri.decode(entry.arguments!!.getString("model")!!)
                        val location = Uri.decode(entry.arguments!!.getString("location")!!)
                        val title = Uri.decode(entry.arguments!!.getString("title")!!)

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
}
