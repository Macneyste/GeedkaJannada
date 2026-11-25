package com.geedkajannada.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.Challenges
import com.geedkajannada.data.DayBar
import com.geedkajannada.data.Dhikrs
import com.geedkajannada.data.StatsSummary
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.ScreenTitle
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.Sun
import com.geedkajannada.ui.theme.CreamPanel
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import com.geedkajannada.ui.theme.IndigoLight
import com.geedkajannada.ui.theme.Line
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoryScreen(vm: HistoryViewModel = appViewModel { HistoryViewModel(it.stats) }) {
    val summary by vm.summary.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.Spacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing),
    ) {
        ScreenTitle(stringResource(R.string.history_title))
        val s = summary
        if (s == null) return@Column
        if (s.perDhikr.values.sum() == 0) {
            GeedkaCard {
                Text(stringResource(R.string.history_empty), style = MaterialTheme.typography.bodyLarge)
            }
            return@Column
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(stringResource(R.string.history_today), s.today.toString(), Modifier.weight(1f))
            StatCard(stringResource(R.string.history_week), s.week.toString(), Modifier.weight(1f))
        }
        StatCard(
            stringResource(R.string.history_streak),
            stringResource(R.string.history_streak_days, s.streak),
            Modifier.fillMaxWidth(),
        )
        ChallengeCard(s)
        WeekChart(s)
        GeedkaCard {
            Text(stringResource(R.string.history_per_dhikr), style = MaterialTheme.typography.titleMedium)
            Dhikrs.all.forEach { d ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(d.nameRes), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        (s.perDhikr[d.id] ?: 0).toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Indigo,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        GeedkaCard {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = Indigo)
        }
    }
}

@Composable
private fun WeekChart(s: StatsSummary) {
    val max = (s.last7.maxOfOrNull { it.total } ?: 0).coerceAtLeast(1)
    val summaryDesc = s.last7.joinToString(", ") { "${it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("so"))}: ${it.total}" }
    GeedkaCard {
        Text(stringResource(R.string.history_last7), style = MaterialTheme.typography.titleMedium)
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(top = 12.dp)
                .semantics { contentDescription = summaryDesc },
        ) {
            val n = s.last7.size
            val gap = 10.dp.toPx()
            val barW = (size.width - gap * (n - 1)) / n
            s.last7.forEachIndexed { i, b: DayBar ->
                val x = i * (barW + gap)
                drawRoundRect(Line, Offset(x, 0f), Size(barW, size.height), CornerRadius(8.dp.toPx()))
                val h = size.height * b.total / max
                if (b.total > 0) {
                    drawRoundRect(IndigoLight, Offset(x, size.height - h), Size(barW, h), CornerRadius(8.dp.toPx()))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            s.last7.forEach {
                Text(
                    it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("so")),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ChallengeCard(s: StatsSummary) {
    val next = Challenges.next(s.bestStreak)
    val achieved = Challenges.achieved(s.bestStreak)
    GeedkaCard {
        Text(stringResource(R.string.challenge_title), style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Challenges.milestones.forEach { m ->
                val done = m in achieved
                androidx.compose.foundation.layout.Box(
                    Modifier
                        .weight(1f)
                        .background(if (done) Sun else CreamPanel, RoundedCornerShape(12.dp))
                        .padding(vertical = 10.dp)
                        .semantics {
                            contentDescription = if (done) "$m: ✓" else "$m"
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        m.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (done) Indigo else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (next != null) {
            Text(
                stringResource(R.string.challenge_next, next, (next - s.streak).coerceAtLeast(0)),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
            LinearProgressIndicator(
                progress = { (s.streak.toFloat() / next).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(10.dp),
                color = IndigoLight,
                trackColor = Line,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        } else {
            Text(
                stringResource(R.string.challenge_all_done),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Text(
            stringResource(R.string.challenge_best, s.bestStreak),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
