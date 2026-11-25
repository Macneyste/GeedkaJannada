package com.geedkajannada.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.geedkajannada.ui.qibla.QiblaScreen
import com.geedkajannada.ui.zakat.ZakatScreen
import com.geedkajannada.ui.quran.ReaderScreen
import com.geedkajannada.ui.settings.SettingsScreen
import com.geedkajannada.ui.theme.CreamPanel
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.Sun
import com.geedkajannada.ui.theme.Sand
import com.geedkajannada.ui.theme.BrownMid

object Routes {
    const val HOME = "home"
    const val QIBLA = "qibla"
    const val ZAKAT = "zakat"
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

private data class Tab(val route: String, val labelRes: Int, @androidx.annotation.DrawableRes val icon: Int)

private val tabs = listOf(
    Tab(Routes.QURAN, R.string.nav_quran, R.drawable.ic_nav_quran),
    Tab(Routes.PRAYER, R.string.nav_prayer, R.drawable.ic_nav_prayer),
    Tab(Routes.HOME, R.string.nav_home, R.drawable.ic_nav_home),
    Tab(Routes.ADHKAR, R.string.nav_adhkar, R.drawable.ic_nav_adhkar),
    Tab(Routes.HISTORY, R.string.nav_history, R.drawable.ic_nav_history),
)

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = tabs.any { it.route == route } || route == Routes.COUNTER

    Scaffold(
        containerColor = CreamPanel,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = CreamPanel) {
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
                            icon = { Icon(androidx.compose.ui.res.painterResource(t.icon), contentDescription = null) },
                            label = { Text(stringResource(t.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CreamPanel,
                                selectedTextColor = Indigo,
                                indicatorColor = Indigo,
                                unselectedIconColor = BrownMid,
                                unselectedTextColor = BrownMid,
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
                    onOpenQibla = { nav.navigate(Routes.QIBLA) },
                    onOpenZakat = { nav.navigate(Routes.ZAKAT) },
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
            composable(Routes.QIBLA) { QiblaScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.ZAKAT) { ZakatScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.PRAYER) { PrayerScreen() }
            composable(Routes.HISTORY) { HistoryScreen() }
        }
    }
}
