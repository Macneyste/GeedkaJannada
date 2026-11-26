package com.geedkajannada.feature.dua

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.StatsCalc
import com.geedkajannada.ui.BackHeader
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.ScreenTitle
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.White
import com.geedkajannada.ui.theme.arabicStyle
import java.time.LocalDate

private fun categoryIcon(id: String): Int = when (id) {
    "subax" -> R.drawable.ic_tile_times
    "safar" -> R.drawable.ic_tile_qibla
    "salaad" -> R.drawable.ic_nav_prayer
    "qoys" -> R.drawable.ic_nav_home
    "rizqi" -> R.drawable.ic_tile_zakat
    ALL_QURAN -> R.drawable.ic_nav_quran
    else -> R.drawable.ic_tile_adhkar
}

@Composable
fun DuaHomeScreen(
    onOpenCategory: (String) -> Unit,
    onOpenDua: (String) -> Unit,
    vm: DuaHomeViewModel = appViewModel { DuaHomeViewModel(it.duas) },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(horizontal = Dimens.ScreenPadding)) {
        ScreenTitle(stringResource(R.string.dua_title), Modifier.padding(top = 8.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::onQuery,
            placeholder = { Text(stringResource(R.string.dua_search)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(Dimens.CardRadiusSmall),
            modifier = Modifier.fillMaxWidth(),
        )
        val tabs = listOf(DuaTab.CATEGORIES to R.string.dua_tab_categories, DuaTab.FAVORITES to R.string.dua_tab_favorites, DuaTab.RECENT to R.string.dua_tab_recent)
        TabRow(selectedTabIndex = state.tab.ordinal, containerColor = Color.Transparent, contentColor = Indigo) {
            tabs.forEach { (tab, label) ->
                Tab(
                    selected = state.tab == tab,
                    onClick = { vm.onTab(tab) },
                    text = { Text(stringResource(label)) },
                    modifier = Modifier.heightIn(min = Dimens.TouchMin),
                )
            }
        }
        when {
            state.loading -> Text(stringResource(R.string.quran_loading), modifier = Modifier.padding(16.dp))
            state.query.isBlank() && state.tab == DuaTab.CATEGORIES -> CategoryGrid(state.categories, onOpenCategory)
            state.duas.isEmpty() -> Text(
                stringResource(if (state.query.isBlank()) R.string.dua_empty else R.string.dua_no_results),
                color = BrownMid,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(24.dp),
            )
            else -> DuaRows(state.duas, onOpenDua)
        }
    }
}

@Composable
private fun CategoryGrid(categories: List<DuaCategory>, onOpen: (String) -> Unit) {
    val all = stringResource(R.string.dua_all_quran)
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(listOf(ALL_QURAN to all) + categories.map { it.id to it.so }, key = { it.first }) { (id, name) ->
            Column(
                Modifier
                    .heightIn(min = 104.dp)
                    .background(White, RoundedCornerShape(16.dp))
                    .border(1.dp, Line, RoundedCornerShape(16.dp))
                    .clickable { onOpen(id) }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(painterResource(categoryIcon(id)), contentDescription = null, tint = Indigo)
                Text(
                    name,
                    style = MaterialTheme.typography.labelMedium,
                    color = BrownInk,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun DuaRows(duas: List<Dua>, onOpen: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(duas, key = { it.id }) { d ->
            GeedkaCard(Modifier.clickable { onOpen(d.id) }) {
                Text(d.so, style = MaterialTheme.typography.bodyLarge, color = BrownInk, maxLines = 2)
                Text(stringResource(R.string.dua_source, d.reference), style = MaterialTheme.typography.labelMedium, color = BrownMid)
            }
        }
    }
}

@Composable
fun DuaListScreen(
    categoryId: String,
    onBack: () -> Unit,
    onOpenDua: (String) -> Unit,
    vm: DuaListViewModel = appViewModel(key = "dualist_$categoryId") { DuaListViewModel(categoryId, it.duas) },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        BackHeader(state.title ?: stringResource(R.string.dua_all_quran), onBack)
        Box(Modifier.padding(horizontal = Dimens.ScreenPadding)) { DuaRows(state.duas, onOpenDua) }
    }
}

@Composable
fun DuaDetailScreen(
    duaId: String,
    onBack: () -> Unit,
    vm: DuaDetailViewModel = appViewModel(key = "dua_$duaId") {
        DuaDetailViewModel(duaId, it.duas, dateKey = { StatsCalc.dateKey(LocalDate.now()) })
    },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dua = state.dua
    // S, M, L, XL
    val arabicSize = listOf(24, 28, 34, 40)[state.textSize.coerceIn(0, 3)]
    val textSize = listOf(14, 16, 19, 22)[state.textSize.coerceIn(0, 3)]

    Column(Modifier.fillMaxSize()) {
        BackHeader(stringResource(R.string.dua_title), onBack)
        if (dua == null) return@Column
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GeedkaCard {
                Text(
                    dua.arabic,
                    style = arabicStyle(arabicSize, quran = true)
                        .copy(textDirection = TextDirection.Rtl, lineHeight = (arabicSize * 2.1f).sp),
                    color = BrownInk,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(dua.so, fontSize = textSize.sp, color = BrownInk, modifier = Modifier.padding(top = 12.dp))
                if (state.showEnglish) {
                    Text(dua.en, fontSize = textSize.sp, color = BrownMid, modifier = Modifier.padding(top = 8.dp))
                }
                Text(
                    stringResource(R.string.dua_source, dua.reference),
                    style = MaterialTheme.typography.labelLarge,
                    color = Indigo,
                    modifier = Modifier.padding(top = 12.dp),
                )
                if (dua.needsReview) {
                    Text(stringResource(R.string.dua_needs_review), style = MaterialTheme.typography.bodySmall, color = BrownMid)
                }
            }

            val done = state.count >= dua.repeat
            Button(
                onClick = vm::count,
                enabled = !done,
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            ) {
                Text(
                    if (done) stringResource(R.string.adhkar_done)
                    else stringResource(R.string.adhkar_tap_count, state.count, dua.repeat),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (state.count > 0) {
                TextButton(onClick = vm::resetCount, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(R.string.reset))
                }
            }

            val shareText = "${dua.arabic}\n\n${dua.so}\n(${context.getString(R.string.dua_source, dua.reference)})"
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { context.copy(shareText) }, modifier = Modifier.weight(1f).heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(R.string.dua_copy))
                }
                OutlinedButton(onClick = { context.share(shareText) }, modifier = Modifier.weight(1f).heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(R.string.reader_share))
                }
                IconButton(onClick = vm::toggleFavorite) {
                    Icon(
                        if (state.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(if (state.favorite) R.string.dua_unfavorite else R.string.dua_favorite),
                        tint = Indigo,
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = vm::toggleEnglish) { Text(stringResource(R.string.dua_english)) }
                Row {
                    listOf("S", "M", "L", "XL").forEachIndexed { i, label ->
                        TextButton(onClick = { vm.setTextSize(i) }) {
                            Text(label, color = if (state.textSize == i) Indigo else BrownMid)
                        }
                    }
                }
            }
        }
    }
}

private fun Context.copy(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("dua", text))
}

private fun Context.share(text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(send, null))
}
