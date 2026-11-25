package com.geedkajannada.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.geedkajannada.R
import com.geedkajannada.ui.adhkar.AdhkarScreen
import com.geedkajannada.ui.counter.CounterScreen
import com.geedkajannada.ui.counter.PickerScreen
import com.geedkajannada.ui.history.HistoryScreen
import com.geedkajannada.ui.home.HomeScreen
import com.geedkajannada.ui.prayer.PrayerScreen
import com.geedkajannada.ui.quran.QuranListScreen
import com.geedkajannada.ui.quran.ReaderScreen
import com.geedkajannada.ui.settings.SettingsScreen
import com.geedkajannada.ui.theme.Cream
import com.geedkajannada.ui.theme.Emerald
import com.geedkajannada.ui.theme.Gold
import com.geedkajannada.ui.theme.Peach
import com.geedkajannada.ui.theme.Bark

object Routes {
    const val HOME = "home"
    const val COUNTER = "counter"
    const val QURAN = "quran"
    const val PRAYER = "prayer"
    const val ADHKAR = "adhkar"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val PICKER = "picker"
    const val SURAH = "surah/{id}?verse={verse}"
    fun surah(id: Int, verse: Int = 1) = "surah/$id?verse=$verse"
}

private data class Tab(val route: String, val labelRes: Int, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, R.string.nav_home, Icons.Filled.Home),
    Tab(Routes.COUNTER, R.string.nav_counter, Icons.Filled.Favorite),
    Tab(Routes.QURAN, R.string.nav_quran, Icons.Filled.Menu),
    Tab(Routes.ADHKAR, R.string.nav_adhkar, Icons.Filled.Star),
    Tab(Routes.PRAYER, R.string.nav_prayer, Icons.Filled.Notifications),
    Tab(Routes.HISTORY, R.string.nav_history, Icons.Filled.DateRange),
)

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = tabs.any { it.route == route }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = Cream) {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = {
                                nav.navigate(t.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(t.icon, contentDescription = null) },
                            label = { Text(stringResource(t.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Cream,
                                selectedTextColor = Emerald,
                                indicatorColor = Emerald,
                                unselectedIconColor = Bark,
                                unselectedTextColor = Bark,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = Routes.HOME, modifier = Modifier.padding(padding)) {
            composable(Routes.HOME) {
                val go = { r: String ->
                    nav.navigate(r) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                HomeScreen(
                    onOpenQuran = { go(Routes.QURAN) },
                    onOpenCounter = { go(Routes.COUNTER) },
                    onOpenPrayer = { go(Routes.PRAYER) },
                    onOpenHistory = { go(Routes.HISTORY) },
                    onOpenAdhkar = { go(Routes.ADHKAR) },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                    onOpenSurah = { id, v -> nav.navigate(Routes.surah(id, v)) },
                )
            }
            composable(Routes.COUNTER) {
                CounterScreen(
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                    onOpenPicker = { nav.navigate(Routes.PICKER) },
                )
            }
            composable(Routes.PICKER) { PickerScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { nav.popBackStack() },
                    onOpenPrayer = {
                        nav.navigate(Routes.PRAYER) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Routes.QURAN) {
                QuranListScreen(onOpenSurah = { id, v -> nav.navigate(Routes.surah(id, v)) })
            }
            composable(
                Routes.SURAH,
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType },
                    navArgument("verse") { type = NavType.IntType; defaultValue = 1 },
                ),
            ) { e ->
                val id = e.arguments?.getInt("id") ?: 1
                val verse = e.arguments?.getInt("verse") ?: 1
                ReaderScreen(
                    surahId = id,
                    startVerse = verse,
                    onBack = { nav.popBackStack() },
                    onOpenSurah = { next ->
                        nav.navigate(Routes.surah(next)) { popUpTo(Routes.QURAN) }
                    },
                )
            }
            composable(Routes.ADHKAR) { AdhkarScreen() }
            composable(Routes.PRAYER) { PrayerScreen() }
            composable(Routes.HISTORY) { HistoryScreen() }
        }
    }
}
