package com.geedkajannada.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.PrayerEntry
import com.geedkajannada.data.PrayerMath
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.Bark
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Emerald
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.Peach
import com.geedkajannada.ui.theme.White
import com.geedkajannada.ui.theme.arabicStyle
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class Tile(val labelRes: Int, val glyph: String, val onClick: () -> Unit)

@Composable
fun HomeScreen(
    onOpenQuran: () -> Unit,
    onOpenCounter: () -> Unit,
    onOpenPrayer: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAdhkar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSurah: (Int, Int) -> Unit,
    vm: HomeViewModel = appViewModel { HomeViewModel(it.prayers, it.settings, it.quran) },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1_000)
        }
    }
    val date = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, color = Emerald)
                Text(date, style = MaterialTheme.typography.bodyMedium, color = Bark)
            }
            TextButton(onClick = onOpenSettings, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                Text(stringResource(R.string.settings))
            }
        }

        val minute = now.hour * 60 + now.minute
        val current = PrayerMath.current(state.entries, minute)
        val next = PrayerMath.next(state.entries, minute, now.second)
        if (current == null || next == null) {
            GeedkaCard(Modifier.clickable(onClick = onOpenPrayer)) {
                Text(stringResource(R.string.home_set_times), style = MaterialTheme.typography.titleMedium, color = Emerald)
            }
        } else {
            PrayerCard(
                stringResource(R.string.home_now),
                current,
                stringResource(R.string.home_ends_in, PrayerMath.formatRemaining(next.second)),
            )
            PrayerCard(stringResource(R.string.home_next), next.first, null)
        }

        val tiles = listOf(
            Tile(R.string.nav_quran, "ق", onOpenQuran),
            Tile(R.string.nav_counter, "سبح", onOpenCounter),
            Tile(R.string.nav_prayer, "ص", onOpenPrayer),
            Tile(R.string.nav_history, "ت", onOpenHistory),
            Tile(R.string.nav_adhkar, "ذ", onOpenAdhkar),
            Tile(R.string.settings, "د", onOpenSettings),
        )
        tiles.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { t ->
                    Column(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 96.dp)
                            .background(Peach, RoundedCornerShape(Dimens.TileRadius))
                            .clickable(onClick = t.onClick)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(t.glyph, style = arabicStyle(26), color = Emerald)
                        Text(stringResource(t.labelRes), style = MaterialTheme.typography.labelLarge, color = Emerald)
                    }
                }
            }
        }

        val last = state.lastRead
        GeedkaCard(
            Modifier.clickable(enabled = last != null) { last?.let { (s, v) -> onOpenSurah(s.id, v) } },
            containerColor = Emerald,
            border = BorderStroke(0.dp, Emerald),
        ) {
            Text(stringResource(R.string.home_continue), style = MaterialTheme.typography.titleMedium, color = White)
            Text(
                if (last == null) stringResource(R.string.home_continue_none)
                else stringResource(R.string.quran_continue_detail, last.first.nameSo, last.second),
                style = MaterialTheme.typography.bodyLarge,
                color = White,
            )
        }
    }
}

@Composable
private fun PrayerCard(label: String, entry: PrayerEntry, extra: String?) {
    GeedkaCard(border = BorderStroke(1.dp, Line)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = Bark)
        Text(
            stringResource(R.string.home_at, stringResource(entry.prayer.nameRes), entry.minuteOfDay?.let { PrayerMath.format(it) } ?: ""),
            style = MaterialTheme.typography.titleLarge,
            color = Emerald,
        )
        if (extra != null) Text(extra, style = MaterialTheme.typography.bodyMedium, color = Bark)
    }
}
