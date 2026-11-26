package com.geedkajannada.ui.adhkar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.AdhkarCatalog
import com.geedkajannada.data.AdhkarPeriod
import com.geedkajannada.data.StatsCalc
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.CreamPanel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.Sand
import com.geedkajannada.ui.theme.White
import com.geedkajannada.ui.theme.arabicStyle
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdhkarScreen(
    vm: AdhkarViewModel = appViewModel { c ->
        AdhkarViewModel(
            c.quran, c.settings,
            dateKey = { StatsCalc.dateKey(LocalDate.now()) },
            initialPeriod = AdhkarCatalog.periodAt(LocalTime.now().hour),
        )
    },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(White)) {
        PeriodBar(state.period, vm::selectPeriod)
        when {
            state.error -> Text(stringResource(R.string.quran_error), modifier = Modifier.padding(Dimens.ScreenPadding))
            state.rows.isEmpty() -> Text(stringResource(R.string.quran_loading), modifier = Modifier.padding(Dimens.ScreenPadding))
            // key: qayb kasta waxay leedahay pager u gaar ah.
            else -> key(state.period) { DhikrPages(state, vm, Modifier.weight(1f)) }
        }
    }
}

/** Madaxa: qaybaha adkaarta (Subax, Masaa, ...). */
@Composable
private fun PeriodBar(selected: AdhkarPeriod, onSelect: (AdhkarPeriod) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Sand)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AdhkarPeriod.entries.forEach { p ->
            val active = p == selected
            Box(
                Modifier
                    .heightIn(min = Dimens.TouchMin)
                    .background(if (active) Indigo else CreamPanel, RoundedCornerShape(14.dp))
                    .border(1.dp, if (active) Indigo else Line, RoundedCornerShape(14.dp))
                    .clickable(role = Role.Tab) { onSelect(p) }
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(p.labelRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (active) White else BrownInk,
                )
            }
        }
    }
}

/** Hal dikri bog kasta (sida mushaf-ka); taabasho meel kasta oo bogga ah waa hal tiro. */
@Composable
private fun DhikrPages(state: AdhkarUiState, vm: AdhkarViewModel, modifier: Modifier) {
    val rows = state.rows
    // Ka bilow dikriga ugu horreeya ee aan weli la dhammayn.
    val pager = rememberPagerState(initialPage = rows.indexOfFirst { !it.done }.coerceAtLeast(0)) { rows.size }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    fun count(index: Int) {
        val row = rows[index]
        if (row.done) return
        val last = row.progress + 1 >= row.item.count
        haptic.performHapticFeedback(if (last) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
        vm.tap(row.item)
        if (last && index < rows.lastIndex) {
            scope.launch {
                delay(450)
                pager.animateScrollToPage(index + 1)
            }
        }
    }

    Column(modifier) {
        Text(
            stringResource(R.string.adhkar_progress, state.doneCount, rows.size),
            style = MaterialTheme.typography.bodyMedium,
            color = BrownMid,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        HorizontalPager(pager, Modifier.weight(1f), key = { rows[it].item.id }) { index ->
            val row = rows[index]
            val tapLabel = stringResource(R.string.adhkar_tap_page)
            Column(
                Modifier
                    .fillMaxSize()
                    .clickable(onClickLabel = tapLabel) { count(index) }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    stringResource(row.item.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = Indigo,
                    textAlign = TextAlign.Center,
                )
                Text(
                    row.arabic,
                    style = arabicStyle(28, quran = true).copy(textDirection = TextDirection.Rtl, lineHeight = 58.sp),
                    color = BrownInk,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(row.item.descRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrownMid,
                    textAlign = TextAlign.Center,
                )
            }
        }
        CounterBar(
            row = rows[pager.currentPage.coerceIn(0, rows.lastIndex)],
            page = pager.currentPage,
            pages = rows.size,
            onCount = { count(pager.currentPage) },
            onReset = { vm.toggle(rows[pager.currentPage].item, false) },
            onPrev = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
            onNext = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
        )
    }
}

/** Hoose: Hore, wareegga tirada, Xiga; iyo dib u bilaabidda dikrigan. */
@Composable
private fun CounterBar(
    row: AdhkarRow,
    page: Int,
    pages: Int,
    onCount: () -> Unit,
    onReset: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Sand)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onPrev, enabled = page > 0, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                Text(stringResource(R.string.adhkar_prev))
            }
            val desc = stringResource(R.string.adhkar_tap_count, row.progress, row.item.count)
            Box(
                Modifier
                    .size(92.dp)
                    .background(if (row.done) Indigo else White, CircleShape)
                    .clickable(role = Role.Button, onClick = onCount)
                    .semantics { contentDescription = desc },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = { row.progress.toFloat() / row.item.count },
                    modifier = Modifier.fillMaxSize(),
                    color = Indigo,
                    trackColor = Line,
                    strokeWidth = 6.dp,
                )
                if (row.done) {
                    Icon(painterResource(R.drawable.ic_check), null, tint = White, modifier = Modifier.size(40.dp))
                } else {
                    Text("${row.progress}/${row.item.count}", style = MaterialTheme.typography.titleLarge, color = BrownInk)
                }
            }
            TextButton(onClick = onNext, enabled = page < pages - 1, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                Text(stringResource(R.string.adhkar_next))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${page + 1} / $pages", style = MaterialTheme.typography.bodyMedium, color = BrownInk)
            TextButton(
                onClick = onReset,
                enabled = row.done || row.progress > 0,
                modifier = Modifier.padding(start = 12.dp).heightIn(min = Dimens.TouchMin),
            ) { Text(stringResource(R.string.reset)) }
        }
    }
}
