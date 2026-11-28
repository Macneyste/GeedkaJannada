package com.geedkajannada.ui.quran

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.Juz
import com.geedkajannada.data.QuranText
import com.geedkajannada.data.SurahText
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.CreamPanel
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.ErrorRed
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.Night
import com.geedkajannada.ui.theme.Sand
import com.geedkajannada.ui.theme.Sun
import com.geedkajannada.ui.theme.White
import com.geedkajannada.ui.theme.arabicStyle
import kotlinx.coroutines.delay

@Composable
fun ReaderScreen(
    surahId: Int,
    startVerse: Int,
    onBack: () -> Unit,
    onOpenSurah: (surahId: Int, verse: Int) -> Unit,
    vm: ReaderViewModel = appViewModel(key = "reader_${surahId}_$startVerse") {
        ReaderViewModel(surahId, startVerse, it.quran, it.settings)
    },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val text = state.text
    val context = LocalContext.current
    var selected by remember { mutableIntStateOf(if (startVerse > 1) startVerse else 0) }
    var auto by remember { mutableStateOf(false) }

    // Item 0 waa madaxa suuradda; ka dib bog kasta wuxuu leeyahay VERSES_PER_BLOCK aayadood.
    val startItem = if (startVerse > 1) QuranText.blockOf(startVerse) + 1 else 0
    LaunchedEffect(text?.surah?.id) {
        if (text == null) return@LaunchedEffect
        if (startItem > 0) listState.scrollToItem(startItem)
        snapshotFlow { listState.firstVisibleItemIndex }.collect { item ->
            vm.onVisibleVerse(if (item == startItem) startVerse else QuranText.firstVerseOfBlock((item - 1).coerceAtLeast(0)))
        }
    }
    LaunchedEffect(auto) {
        while (auto) {
            if (listState.scrollBy(1.5f) == 0f) auto = false
            delay(16)
        }
    }

    /** Aayadda ficilladu khuseeyaan: tan la doortay, haddii kale tan ugu horreysa ee muuqata. */
    fun target(): Int =
        if (selected > 0) selected
        else QuranText.firstVerseOfBlock((listState.firstVisibleItemIndex - 1).coerceAtLeast(0))

    val page = if (state.dark) Night else White
    val ink = if (state.dark) CreamPanel else BrownInk

    Column(Modifier.fillMaxSize().background(page)) {
        ReaderTopBar(state, selected, onBack, onOpenSurah)

        when {
            state.error -> Center(Modifier.weight(1f)) { Text(stringResource(R.string.quran_error), color = ink) }
            text == null -> Center(Modifier.weight(1f)) { Text(stringResource(R.string.quran_loading), color = ink) }
            else -> CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MushafText(
                    text = text,
                    bismillah = state.bismillah,
                    fontSize = state.fontSize,
                    ink = ink,
                    highlight = if (state.dark) Sun else ErrorRed,
                    selected = selected,
                    listState = listState,
                    onSelect = {
                        selected = it
                        vm.onVisibleVerse(it)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ReaderToolbar(
            fontSize = state.fontSize,
            playing = auto,
            bookmarked = state.bookmarkedVerse != null,
            onFontSize = vm::setFontSize,
            onPlay = { auto = !auto },
            onBookmark = { vm.toggleBookmark(state.bookmarkedVerse ?: target()) },
            onShare = share@{
                val t = text ?: return@share
                val v = target().coerceIn(1, t.verses.size)
                val body = context.getString(R.string.reader_share_text, t.verses[v - 1], t.surah.nameSo, t.surah.id, v)
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                context.startActivity(Intent.createChooser(send, null))
            },
            onTheme = vm::toggleDark,
        )
    }
}

@Composable
private fun Center(modifier: Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { content() }
}

/** Madaxa: Dib, doorashada Juz, doorashada suuradda. */
@Composable
private fun ReaderTopBar(
    state: ReaderUiState,
    selected: Int,
    onBack: () -> Unit,
    onOpenSurah: (Int, Int) -> Unit,
) {
    var juzMenu by remember { mutableStateOf(false) }
    var surahMenu by remember { mutableStateOf(false) }
    val surah = state.text?.surah
    val juz = surah?.let { Juz.of(it.id, selected.coerceAtLeast(state.startVerse)) } ?: 1

    Row(
        Modifier
            .fillMaxWidth()
            .background(Sand)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(Dimens.TouchMin)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = BrownInk)
        }
        Pill(stringResource(R.string.quran_juz_n, juz), { juzMenu = true }) {
            DropdownMenu(expanded = juzMenu, onDismissRequest = { juzMenu = false }) {
                Juz.starts.forEachIndexed { i, (s, v) ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.quran_juz_n, i + 1)) },
                        onClick = {
                            juzMenu = false
                            onOpenSurah(s, v)
                        },
                    )
                }
            }
        }
        Pill(surah?.nameSo ?: "", { surahMenu = true }) {
            DropdownMenu(expanded = surahMenu, onDismissRequest = { surahMenu = false }) {
                state.surahs.forEach { s ->
                    DropdownMenuItem(
                        text = { Text("${s.id}. ${s.nameSo}") },
                        onClick = {
                            surahMenu = false
                            onOpenSurah(s.id, 1)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.Pill(label: String, onClick: () -> Unit, menu: @Composable () -> Unit) {
    Box(
        Modifier
            .weight(1f)
            .heightIn(min = Dimens.TouchMin)
            .background(CreamPanel, RoundedCornerShape(14.dp))
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = BrownInk, maxLines = 1)
        menu()
    }
}

/** Qoraalka oo si dabiici ah u socda (mushaf), lambarka aayadda goobo; aayadda la taabto way iftiintaa. */
@Composable
private fun MushafText(
    text: SurahText,
    bismillah: String?,
    fontSize: Int,
    ink: Color,
    highlight: Color,
    selected: Int,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
) {
    val blocks = remember(text.surah.id) { QuranText.blocks(text.verses.size) }
    val style = arabicStyle(fontSize, quran = true).copy(
        textDirection = TextDirection.Rtl,
        lineHeight = (fontSize * 2.1f).sp,
        color = ink,
    )
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        item(key = "head") {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("سُورَةُ ${text.surah.nameAr}", style = arabicStyle(fontSize, quran = true), color = ink)
                if (bismillah != null) {
                    Text(bismillah, style = style, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        itemsIndexed(blocks, key = { _, range -> range.first }) { _, range ->
            val annotated = buildAnnotatedString {
                for (n in range) {
                    val color = if (n == selected) highlight else ink
                    val link = LinkAnnotation.Clickable("v$n", TextLinkStyles(SpanStyle())) { onSelect(n) }
                    withLink(link) {
                        withStyle(SpanStyle(color = color)) {
                            append(text.verses[n - 1])
                            append(' ')
                            // Goobada lambarka: app-ka ayaa sawira, kuma xirna farta telefoonka.
                            appendInlineContent("v$n", QuranText.arabicDigits(n))
                        }
                    }
                    append(' ')
                }
            }
            val markers = range.associate { n ->
                "v$n" to InlineTextContent(
                    Placeholder((fontSize * 1.25f).sp, (fontSize * 1.25f).sp, PlaceholderVerticalAlign.Center),
                ) { VerseMarker(n, fontSize, if (n == selected) highlight else ink) { onSelect(n) } }
            }
            Text(annotated, style = style, textAlign = TextAlign.Center, inlineContent = markers, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** Goobo leh lambarka aayadda oo dhexda ku jira. */
@Composable
private fun VerseMarker(number: Int, fontSize: Int, color: Color, onClick: () -> Unit) {
    // Saddex lambar (tusaale 286) waxay u baahan yihiin far yar.
    val digitSize = fontSize * when {
        number < 10 -> 0.74f
        number < 100 -> 0.64f
        else -> 0.5f
    }
    Box(
        Modifier
            .fillMaxSize()
            .padding(1.dp)
            .border(1.5.dp, color, CircleShape)
            .padding(2.dp)
            .border(0.7.dp, color.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            QuranText.arabicDigits(number),
            style = TextStyle(
                fontSize = digitSize.sp,
                lineHeight = digitSize.sp,
                color = color,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun ReaderToolbar(
    fontSize: Int,
    playing: Boolean,
    bookmarked: Boolean,
    onFontSize: (Int) -> Unit,
    onPlay: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onTheme: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Sand)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val smaller = stringResource(R.string.reader_smaller)
            val bigger = stringResource(R.string.reader_bigger)
            TextButton(onClick = { onFontSize(fontSize - 2) }, modifier = Modifier.size(Dimens.TouchMin)) {
                Text("A−", color = BrownInk)
            }
            Slider(
                value = fontSize.toFloat(),
                onValueChange = { onFontSize(it.toInt()) },
                valueRange = 18f..48f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(thumbColor = Indigo, activeTrackColor = Indigo, inactiveTrackColor = White),
            )
            TextButton(onClick = { onFontSize(fontSize + 2) }, modifier = Modifier.size(Dimens.TouchMin)) {
                Text("A+", color = BrownInk)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ToolButton(stringResource(if (playing) R.string.reader_auto_stop else R.string.reader_auto), onPlay) {
                Icon(painterResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play), null, tint = Indigo)
            }
            ToolButton(stringResource(if (bookmarked) R.string.bookmark_remove else R.string.bookmark_add), onBookmark) {
                Icon(Icons.Filled.Star, null, tint = if (bookmarked) Sun else Line)
            }
            ToolButton(stringResource(R.string.reader_share), onShare) {
                Icon(painterResource(R.drawable.ic_share), null, tint = Indigo)
            }
            ToolButton(stringResource(R.string.reader_theme), onTheme) {
                Icon(painterResource(R.drawable.ic_theme), null, tint = Indigo)
            }
        }
    }
}

@Composable
private fun ToolButton(label: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(Dimens.TouchMin)
            .background(White, CircleShape),
    ) {
        Box(Modifier.semanticsLabel(label)) { icon() }
    }
}

private fun Modifier.semanticsLabel(label: String): Modifier =
    this.then(Modifier.semantics { contentDescription = label })
