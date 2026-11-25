package com.geedkajannada.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.Hijri
import com.geedkajannada.data.HijriMonthNames
import com.geedkajannada.data.Prayer
import com.geedkajannada.data.PrayerEntry
import com.geedkajannada.data.PrayerMath
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.Bronze
import com.geedkajannada.ui.theme.Sand
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.IndigoLight
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.Night
import com.geedkajannada.ui.theme.Sun
import com.geedkajannada.ui.theme.White
import com.geedkajannada.ui.theme.arabicStyle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private data class Tile(val labelRes: Int, val subRes: Int, val glyph: String, val onClick: () -> Unit)

@Composable
fun HomeScreen(
    onOpenQuran: () -> Unit,
    onOpenCounter: () -> Unit,
    onOpenPrayer: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAdhkar: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenZakat: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSurah: (Int, Int) -> Unit,
    vm: HomeViewModel = appViewModel { HomeViewModel(it.prayers, it.settings, it.quran, it.stats) },
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
    val hijri = Hijri.of(now.toLocalDate())
    val hijriText = stringResource(R.string.hijri_format, hijri.day, stringResource(HijriMonthNames[hijri.month - 1]), hijri.year)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.HomePadding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, color = BrownInk)
                Text(stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodyMedium, color = BrownMid)
            }
            androidx.compose.material3.IconButton(onClick = onOpenSettings, modifier = Modifier.size(Dimens.TouchMin)) {
                androidx.compose.material3.Icon(
                    Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.home_gear),
                    tint = Indigo,
                )
            }
        }

        val minute = now.hour * 60 + now.minute
        val current = PrayerMath.current(state.entries, minute)
        val next = PrayerMath.next(state.entries, minute, now.second)
        HeroCard(
            date = date,
            hijri = hijriText,
            current = current,
            next = next?.first,
            remaining = next?.second,
            streak = state.streak,
            entries = state.entries,
            onSetTimes = onOpenPrayer,
        )

        PrayerTrackerCard(state.entries, state.checked, vm::toggleChecked, onOpenPrayer)

        val last = state.lastRead
        GeedkaCard(
            Modifier.clickable(enabled = last != null) { last?.let { (s, v) -> onOpenSurah(s.id, v) } },
            containerColor = Night,
            border = BorderStroke(0.dp, Night),
        ) {
            Text(stringResource(R.string.home_continue), style = MaterialTheme.typography.labelLarge, color = Sun)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (last == null) stringResource(R.string.home_continue_none)
                    else stringResource(R.string.quran_continue_detail, last.first.nameSo, last.second),
                    style = MaterialTheme.typography.titleMedium,
                    color = White,
                    modifier = Modifier.weight(1f),
                )
                if (last != null) Text(stringResource(R.string.home_continue_btn), style = MaterialTheme.typography.titleMedium, color = Sun)
            }
        }

        val tiles = listOf(
            Tile(R.string.nav_quran, R.string.tile_quran_sub, "ق", onOpenQuran),
            Tile(R.string.nav_qibla, R.string.tile_qibla_sub, "ك", onOpenQibla),
            Tile(R.string.nav_tasbeeh, R.string.tile_tasbeeh_sub, "سبح", onOpenCounter),
            Tile(R.string.nav_adhkar, R.string.tile_adhkar_sub, "ذ", onOpenAdhkar),
            Tile(R.string.nav_times, R.string.tile_times_sub, "ص", onOpenPrayer),
            Tile(R.string.nav_history, R.string.tile_history_sub, "ت", onOpenHistory),
            Tile(R.string.nav_zakat, R.string.tile_zakat_sub, "ز", onOpenZakat),
            Tile(R.string.settings, R.string.tile_settings_sub, "د", onOpenSettings),
        )
        tiles.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { t ->
                    Column(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 92.dp)
                            .background(White, RoundedCornerShape(Dimens.TileRadius))
                            .border(1.dp, Line, RoundedCornerShape(Dimens.TileRadius))
                            .clickable(onClick = t.onClick)
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(t.glyph, style = arabicStyle(24), color = Indigo)
                        Text(stringResource(t.labelRes), style = MaterialTheme.typography.labelMedium, color = BrownInk, maxLines = 1)
                        Text(stringResource(t.subRes), style = MaterialTheme.typography.labelSmall, color = BrownMid, maxLines = 1)
                    }
                }
            }
        }

        GeedkaCard(Modifier.clickable(onClick = onOpenAdhkar)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.home_adhkar_card, state.subaxDone, state.subaxTotal),
                    style = MaterialTheme.typography.titleMedium,
                    color = BrownInk,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    stringResource(if (state.subaxDone == 0) R.string.home_adhkar_start else R.string.home_adhkar_go),
                    style = MaterialTheme.typography.titleMedium,
                    color = Indigo,
                )
            }
        }
    }
}

@Composable
private fun HeroCard(
    date: String,
    hijri: String,
    current: PrayerEntry?,
    next: PrayerEntry?,
    remaining: Int?,
    streak: Int,
    entries: List<PrayerEntry>,
    onSetTimes: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 190.dp)
            .background(Brush.linearGradient(listOf(Indigo, IndigoLight)), RoundedCornerShape(Dimens.HeroRadius))
            .clip(RoundedCornerShape(Dimens.HeroRadius)),
    ) {
        // Qorraxda iyo masjidka: qaab nala leh (Canvas), ma aha sawir la koobiyay.
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawCircle(Sun, radius = h * 0.26f, center = Offset(w * 0.68f, h * 0.50f))
            val base = h * 0.98f
            // dhismaha hoose
            drawRect(Night, Offset(w * 0.40f, base - h * 0.16f), Size(w * 0.60f, h * 0.16f))
            // qubbad weyn
            drawArc(Night, 180f, 180f, true, Offset(w * 0.55f, base - h * 0.42f), Size(w * 0.26f, h * 0.52f))
            // minaarad
            drawRect(Night, Offset(w * 0.47f, base - h * 0.46f), Size(w * 0.035f, h * 0.46f))
            drawRect(Night, Offset(w * 0.86f, base - h * 0.46f), Size(w * 0.035f, h * 0.46f))
            drawCircle(Night, h * 0.03f, Offset(w * 0.4875f, base - h * 0.47f))
            drawCircle(Night, h * 0.03f, Offset(w * 0.8775f, base - h * 0.47f))
            // qubbad yar
            drawArc(Night, 180f, 180f, true, Offset(w * 0.91f, base - h * 0.28f), Size(w * 0.10f, h * 0.30f))
            // dhulka
            drawRect(Night, Offset(0f, base), Size(w, h - base))
        }
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(date, style = MaterialTheme.typography.bodyMedium, color = White)
            Text(hijri, style = MaterialTheme.typography.labelLarge, color = White)
            Text(
                stringResource(R.string.home_streak, streak),
                style = MaterialTheme.typography.labelLarge,
                color = Night,
                modifier = Modifier
                    .background(Sun, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
            if (current == null || next == null || remaining == null) {
                Text(
                    stringResource(R.string.home_set_times),
                    style = MaterialTheme.typography.titleMedium,
                    color = Sun,
                    modifier = Modifier
                        .heightIn(min = Dimens.TouchMin)
                        .clickable(onClick = onSetTimes)
                        .padding(top = 12.dp),
                )
            } else {
                Text(
                    stringResource(R.string.home_next),
                    style = MaterialTheme.typography.labelLarge,
                    color = White,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Text(stringResource(next.prayer.nameRes), style = MaterialTheme.typography.titleLarge, color = White)
                Text(
                    next.minuteOfDay?.let { PrayerMath.format(it) } ?: "",
                    style = MaterialTheme.typography.displaySmall,
                    color = White,
                )
                Text(
                    stringResource(R.string.home_now) + ": " +
                        stringResource(R.string.home_at, stringResource(current.prayer.nameRes), current.minuteOfDay?.let { PrayerMath.format(it) } ?: ""),
                    style = MaterialTheme.typography.titleSmall,
                    color = Sun,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    stringResource(R.string.home_ends_in, PrayerMath.formatRemaining(remaining)),
                    style = MaterialTheme.typography.bodySmall,
                    color = White,
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(Night.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    entries.forEach { e ->
                        val isNext = e.prayer == next.prayer
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                stringResource(e.prayer.nameRes),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isNext) Sun else White,
                            )
                            Text(
                                e.minuteOfDay?.let { PrayerMath.format(it) } ?: "--:--",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isNext) Sun else White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerTrackerCard(
    entries: List<PrayerEntry>,
    checked: Set<Prayer>,
    onToggle: (Prayer) -> Unit,
    onShow: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(Dimens.CardRadius))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.home_prayer_tracker),
                style = MaterialTheme.typography.titleMedium,
                color = BrownInk,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onShow, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                Text(stringResource(R.string.home_show), color = Indigo)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Prayer.entries.forEach { p ->
                val done = p in checked
                val name = stringResource(p.nameRes)
                val desc = stringResource(if (done) R.string.home_prayer_done else R.string.home_prayer_todo, name)
                val time = entries.firstOrNull { it.prayer == p }?.minuteOfDay?.let { PrayerMath.format(it) } ?: "--:--"
                Column(
                    Modifier
                        .clickable { onToggle(p) }
                        .heightIn(min = Dimens.TouchMin)
                        .semantics { contentDescription = desc },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .background(if (done) Bronze else White, CircleShape)
                            .border(2.dp, if (done) Bronze else BrownMid, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (done) Text("✓", color = White, style = MaterialTheme.typography.titleMedium)
                    }
                    Text(name, style = MaterialTheme.typography.labelMedium, color = BrownInk)
                    Text(time, style = MaterialTheme.typography.labelSmall, color = BrownMid)
                }
            }
        }
    }
}
