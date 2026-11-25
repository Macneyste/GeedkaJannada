package com.geedkajannada.ui.quran

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.geedkajannada.ui.theme.Cream
import com.geedkajannada.ui.theme.Ink
import kotlinx.coroutines.delay
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.ui.ScreenTitle
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Emerald
import com.geedkajannada.ui.theme.Gold
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.Peach
import com.geedkajannada.ui.theme.White
import com.geedkajannada.ui.theme.arabicStyle
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.HorizontalDivider

@Composable
fun ReaderScreen(
    surahId: Int,
    startVerse: Int,
    onBack: () -> Unit,
    onOpenSurah: (Int) -> Unit,
    vm: ReaderViewModel = appViewModel(key = "reader_$surahId") {
        ReaderViewModel(surahId, startVerse, it.quran, it.settings)
    },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val text = state.text

    LaunchedEffect(text?.surah?.id) {
        if (text != null && startVerse > 1) listState.scrollToItem((startVerse - 1).coerceIn(0, text.verses.size - 1))
    }
    LaunchedEffect(text?.surah?.id) {
        if (text != null) {
            snapshotFlow { listState.firstVisibleItemIndex }.collect { vm.onVisibleVerse(it + 1) }
        }
    }

    var auto by remember { mutableStateOf(false) }
    LaunchedEffect(auto) {
        while (auto) {
            val moved = listState.scrollBy(1.5f)
            if (moved == 0f) auto = false
            delay(16)
        }
    }
    val ctx = LocalContext.current
    val bg = if (state.dark) Ink else Cream
    val fg = if (state.dark) Cream else Ink

    Column(Modifier.fillMaxSize().background(bg)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text?.surah?.nameSo ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    color = fg,
                )
                text?.let {
                    Text(
                        stringResource(R.string.reader_header, com.geedkajannada.data.Juz.of(it.surah.id, listState.firstVisibleItemIndex + 1), it.surah.id, it.surah.verseCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = fg,
                    )
                }
            }
            val smallerDesc = stringResource(R.string.reader_smaller)
            val biggerDesc = stringResource(R.string.reader_bigger)
            TextButton(
                onClick = vm::smaller,
                modifier = Modifier
                    .heightIn(min = Dimens.TouchMin)
                    .semantics { contentDescription = smallerDesc },
            ) { Text("A−", color = fg) }
            TextButton(
                onClick = vm::bigger,
                modifier = Modifier
                    .heightIn(min = Dimens.TouchMin)
                    .semantics { contentDescription = biggerDesc },
            ) { Text("A+", color = fg) }
            val firstVisible = listState.firstVisibleItemIndex + 1
            val marked = state.bookmarkedVerse != null
            IconButton(
                onClick = { vm.toggleBookmark(state.bookmarkedVerse ?: firstVisible) },
                modifier = Modifier.heightIn(min = Dimens.TouchMin),
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = stringResource(if (marked) R.string.bookmark_remove else R.string.bookmark_add),
                    tint = if (marked) Gold else Line,
                )
            }
        }
        HorizontalDivider(color = Line)

        when {
            state.error -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.quran_error), modifier = Modifier.padding(Dimens.ScreenPadding))
            }
            text == null -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.quran_loading))
            }
            else -> CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = Dimens.ScreenPadding, vertical = Dimens.Spacing,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(text.verses, key = { i, _ -> i }) { i, verse ->
                        val n = i + 1
                        val marked = state.bookmarkedVerse == n
                        val verseDesc = stringResource(R.string.verse_number, n)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(if (marked) Peach else if (state.dark) Emerald else White, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                verse,
                                style = arabicStyle(state.fontSize, quran = true).copy(textDirection = TextDirection.Rtl),
                                color = if (state.dark) Cream else Ink,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1f),
                            )
                            Box(
                                Modifier
                                    .padding(start = 10.dp)
                                    .size(Dimens.TouchMin - 8.dp)
                                    .background(if (marked) Gold else Peach, CircleShape)
                                    .clickable(
                                        onClickLabel = stringResource(
                                            if (marked) R.string.bookmark_remove else R.string.bookmark_add,
                                        ),
                                    ) { vm.toggleBookmark(n) }
                                    .semantics { contentDescription = verseDesc },
                                contentAlignment = Alignment.Center,
                            ) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Text(n.toString(), style = MaterialTheme.typography.labelLarge, color = Emerald)
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Line)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = Dimens.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = {
                    val t = text ?: return@TextButton
                    val v = (listState.firstVisibleItemIndex + 1).coerceIn(1, t.verses.size)
                    val body = ctx.getString(R.string.reader_share_text, t.verses[v - 1], t.surah.nameSo, t.surah.id, v)
                    val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, body) }
                    ctx.startActivity(Intent.createChooser(send, null))
                },
                modifier = Modifier.weight(1f).heightIn(min = Dimens.TouchMin),
            ) { Text(stringResource(R.string.reader_share), color = if (state.dark) Cream else Emerald) }
            TextButton(
                onClick = { auto = !auto },
                modifier = Modifier.weight(1f).heightIn(min = Dimens.TouchMin),
            ) { Text(stringResource(if (auto) R.string.reader_auto_stop else R.string.reader_auto), color = if (state.dark) Cream else Emerald) }
            TextButton(
                onClick = vm::toggleDark,
                modifier = Modifier.weight(1f).heightIn(min = Dimens.TouchMin),
            ) { Text(stringResource(R.string.reader_theme), color = if (state.dark) Cream else Emerald) }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = { onOpenSurah(surahId - 1) },
                enabled = state.hasPrev,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Dimens.TouchMin),
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                Text(stringResource(R.string.reader_prev))
            }
            OutlinedButton(
                onClick = { onOpenSurah(surahId + 1) },
                enabled = state.hasNext,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Dimens.TouchMin),
            ) {
                Text(stringResource(R.string.reader_next))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}
