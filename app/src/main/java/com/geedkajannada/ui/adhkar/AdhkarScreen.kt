package com.geedkajannada.ui.adhkar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.AdhkarCatalog
import com.geedkajannada.data.AdhkarPeriod
import com.geedkajannada.data.StatsCalc
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.ScreenTitle
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Emerald
import com.geedkajannada.ui.theme.Leaf
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.arabicStyle
import java.time.LocalDate
import java.time.LocalTime

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
    val shape = RoundedCornerShape(Dimens.CardRadiusSmall)
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenPadding),
    ) {
        ScreenTitle(stringResource(R.string.adhkar_title), Modifier.padding(top = Dimens.Spacing))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AdhkarPeriod.entries.forEach { p ->
                val mod = Modifier.heightIn(min = Dimens.TouchMin)
                if (state.period == p) {
                    Button(onClick = { vm.selectPeriod(p) }, modifier = mod, shape = shape) {
                        Text(stringResource(p.labelRes))
                    }
                } else {
                    OutlinedButton(onClick = { vm.selectPeriod(p) }, modifier = mod, shape = shape) {
                        Text(stringResource(p.labelRes))
                    }
                }
            }
        }
        Text(
            stringResource(R.string.adhkar_progress, state.doneCount, state.rows.size),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        if (state.error) {
            Text(stringResource(R.string.quran_error), modifier = Modifier.padding(Dimens.ScreenPadding))
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            items(state.rows, key = { it.item.id }) { row ->
                GeedkaCard(border = if (row.done) BorderStroke(2.dp, Emerald) else BorderStroke(1.dp, Line)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(row.item.titleRes),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            stringResource(R.string.adhkar_times, row.item.count),
                            style = MaterialTheme.typography.titleMedium,
                            color = Emerald,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            row.arabic,
                            style = arabicStyle(26, quran = true).copy(textDirection = TextDirection.Rtl),
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                        )
                    }
                    Text(
                        stringResource(row.item.descRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    val btn = Modifier
                        .fillMaxWidth()
                        .heightIn(min = Dimens.TouchMin)
                    when {
                        row.done -> Button(onClick = { vm.toggle(row.item, false) }, modifier = btn, shape = shape) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(R.string.adhkar_done))
                        }
                        row.item.count == 1 -> OutlinedButton(
                            onClick = { vm.toggle(row.item, true) },
                            modifier = btn,
                            shape = shape,
                        ) { Text(stringResource(R.string.adhkar_mark_done)) }
                        else -> {
                            LinearProgressIndicator(
                                progress = { row.progress.toFloat() / row.item.count },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .heightIn(min = 8.dp),
                                color = Leaf,
                                trackColor = Line,
                            )
                            OutlinedButton(onClick = { vm.tap(row.item) }, modifier = btn, shape = shape) {
                                Text(stringResource(R.string.adhkar_tap_count, row.progress, row.item.count))
                            }
                        }
                    }
                }
            }
        }
    }
}
