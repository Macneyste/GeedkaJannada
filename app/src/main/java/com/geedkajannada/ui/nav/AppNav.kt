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
import com.geedkajannada.feature.dua.DuaDetailScreen
import com.geedkajannada.feature.dua.DuaHomeScreen
import com.geedkajannada.feature.dua.DuaListScreen
import com.geedkajannada.feature.names.NamesScreen
import com.geedkajannada.feature.tasbeeh.TasbeehScreen
import com.geedkajannada.ui.more.AboutScreen
import com.geedkajannada.ui.more.MoreItem
import com.geedkajannada.ui.more.MoreScreen
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
    const val DUA = "dua"
    const val DUA_LIST = "dualist/{id}"
    const val DUA_ITEM = "duaitem/{id}"
    const val MORE = "more"
    const val NAMES = "names"
    const val ABOUT = "about"
    fun duaList(id: String) = "dualist/$id"
    fun duaItem(id: String) = "duaitem/$id"
    const val SURAH = "surah/{id}?verse={verse}"
    fun surah(id: Int, verse: Int = 1) = "surah/$id?verse=$verse"
}

private data class Tab(val route: String, val labelRes: Int, @androidx.annotation.DrawableRes val icon: Int)

private val tabs = listOf(
    Tab(Routes.QURAN, R.string.nav_quran, R.drawable.ic_nav_quran),
    Tab(Routes.PRAYER, R.string.nav_prayer, R.drawable.ic_nav_prayer),
    Tab(Routes.HOME, R.string.nav_home, R.drawable.ic_nav_home),
    Tab(Routes.DUA, R.string.nav_dua, R.drawable.ic_nav_adhkar),
    Tab(Routes.NAMES, R.string.nav_names, R.drawable.ic_nav_names),
)

/** Bogagga hoose ee aan lahayn badhan "Dib": bar-ku wuu sii muuqdaa. */
private val subPagesWithBar = setOf(Routes.COUNTER, Routes.ADHKAR, Routes.HISTORY)

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = tabs.any { it.route == route } || route in subPagesWithBar

    com.geedkajannada.ui.location.LocationGate()
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
                            label = {
                                // Magacyada dheer (Magacyada Alle) waxay galaan laba saf.
                                Text(stringResource(t.labelRes), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            },
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
                val tab = { r: String ->
                    nav.navigate(r) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                HomeScreen(
                    onOpenQibla = { nav.navigate(Routes.QIBLA) },
                    onOpenCounter = { nav.navigate(Routes.COUNTER) },
                    onOpenDua = { tab(Routes.DUA) },
                    onOpenZakat = { nav.navigate(Routes.ZAKAT) },
                    onOpenPrayer = { tab(Routes.PRAYER) },
                    onOpenMore = { nav.navigate(Routes.MORE) },
                    onOpenSurah = { id, v -> nav.navigate(Routes.surah(id, v)) },
                )
            }
            composable(Routes.DUA) {
                DuaHomeScreen(
                    onOpenCategory = { nav.navigate(Routes.duaList(it)) },
                    onOpenDua = { nav.navigate(Routes.duaItem(it)) },
                )
            }
            composable(Routes.DUA_LIST) { e ->
                DuaListScreen(
                    categoryId = e.arguments?.getString("id").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onOpenDua = { nav.navigate(Routes.duaItem(it)) },
                )
            }
            composable(Routes.DUA_ITEM) { e ->
                DuaDetailScreen(duaId = e.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() })
            }
            composable(Routes.NAMES) { NamesScreen() }
            composable(Routes.MORE) {
                MoreScreen(
                    onBack = { nav.popBackStack() },
                    items =
                    listOf(
                        MoreItem(R.string.nav_tasbeeh, R.drawable.ic_nav_adhkar) { nav.navigate(Routes.COUNTER) },
                        MoreItem(R.string.nav_qibla, R.drawable.ic_tile_qibla) { nav.navigate(Routes.QIBLA) },
                        MoreItem(R.string.nav_zakat, R.drawable.ic_tile_zakat) { nav.navigate(Routes.ZAKAT) },
                        MoreItem(R.string.nav_adhkar, R.drawable.ic_tile_adhkar) { nav.navigate(Routes.ADHKAR) },
                        MoreItem(R.string.nav_history, R.drawable.ic_nav_history) { nav.navigate(Routes.HISTORY) },
                        MoreItem(R.string.settings, R.drawable.ic_tile_settings) { nav.navigate(Routes.SETTINGS) },
                        MoreItem(R.string.about_title, R.drawable.ic_nav_home) { nav.navigate(Routes.ABOUT) },
                    ),
                )
            }
            composable(Routes.ABOUT) { AboutScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.COUNTER) {
                TasbeehScreen()
            }
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
                    onOpenSurah = { next, v ->
                        nav.navigate(Routes.surah(next, v)) { popUpTo(Routes.SURAH) { inclusive = true } }
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
