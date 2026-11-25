package com.geedkajannada.ui.quran

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import com.geedkajannada.data.Juz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.Surah
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.ScreenTitle
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Emerald
import com.geedkajannada.ui.theme.Peach
import com.geedkajannada.ui.theme.arabicStyle
import com.geedkajannada.ui.theme.White

@Composable
fun QuranListScreen(
    onOpenSurah: (surahId: Int, verse: Int) -> Unit,
    vm: QuranListViewModel = appViewModel { QuranListViewModel(it.quran, it.settings) },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenPadding),
    ) {
        ScreenTitle(stringResource(R.string.quran_title), Modifier.padding(top = Dimens.Spacing))
        TabRow(
            selectedTabIndex = if (state.juzTab) 1 else 0,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = Emerald,
        ) {
            Tab(selected = !state.juzTab, onClick = { vm.showJuz(false) }, text = { Text(stringResource(R.string.quran_tab_surah)) }, modifier = Modifier.heightIn(min = Dimens.TouchMin))
            Tab(selected = state.juzTab, onClick = { vm.showJuz(true) }, text = { Text(stringResource(R.string.quran_tab_juz)) }, modifier = Modifier.heightIn(min = Dimens.TouchMin))
        }
        if (!state.juzTab) OutlinedTextField(
            value = state.query,
            onValueChange = vm::onQuery,
            placeholder = { Text(stringResource(R.string.quran_search)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(Dimens.CardRadiusSmall),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )
        when {
            state.error -> Message(stringResource(R.string.quran_error))
            state.loading -> Message(stringResource(R.string.quran_loading))
            state.juzTab -> JuzList(state.allSurahs, onOpenSurah)
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = Dimens.Spacing),
            ) {
                state.continueReading?.let { (surah, verse) ->
                    if (state.query.isBlank()) item(key = "continue") {
                        ContinueCard(surah, verse) { onOpenSurah(surah.id, verse) }
                    }
                }
                if (state.surahs.isEmpty()) {
                    item(key = "empty") { Message(stringResource(R.string.quran_empty)) }
                }
                items(state.surahs, key = { it.id }) { s -> SurahRow(s) { onOpenSurah(s.id, 1) } }
            }
        }
    }
}

@Composable
private fun Message(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.ScreenPadding),
    )
}

@Composable
private fun ContinueCard(surah: Surah, verse: Int, onClick: () -> Unit) {
    GeedkaCard(
        modifier = Modifier.clickable(onClick = onClick),
        containerColor = Emerald,
        border = androidx.compose.foundation.BorderStroke(0.dp, Emerald),
    ) {
        Text(stringResource(R.string.quran_continue), style = MaterialTheme.typography.titleMedium, color = White)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.quran_continue_detail, surah.nameSo, verse),
                style = MaterialTheme.typography.bodyLarge,
                color = White,
                modifier = Modifier.weight(1f),
            )
            Text(surah.nameAr, style = arabicStyle(24), color = White)
        }
    }
}

@Composable
private fun SurahRow(s: Surah, onClick: () -> Unit) {
    GeedkaCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(Modifier.heightIn(min = Dimens.TouchMin), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(Peach, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(s.id.toString(), style = MaterialTheme.typography.labelLarge, color = Emerald)
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(s.nameSo, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.quran_verses, s.verseCount) + " · " +
                        stringResource(if (s.isMakki) R.string.makki else R.string.madani),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(s.nameAr, style = arabicStyle(24), color = Emerald)
        }
    }
}

@Composable
private fun JuzList(surahs: List<Surah>, onOpenSurah: (Int, Int) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = Dimens.Spacing),
    ) {
        items(Juz.COUNT, key = { it }) { i ->
            val (sid, verse) = Juz.starts[i]
            val name = surahs.firstOrNull { it.id == sid }?.nameSo ?: sid.toString()
            GeedkaCard(modifier = Modifier.clickable { onOpenSurah(sid, verse) }) {
                Row(Modifier.heightIn(min = Dimens.TouchMin), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).background(Peach, CircleShape), contentAlignment = Alignment.Center) {
                        Text((i + 1).toString(), style = MaterialTheme.typography.labelLarge, color = Emerald)
                    }
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(stringResource(R.string.quran_juz_n, i + 1), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.quran_juz_starts, name, verse),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
