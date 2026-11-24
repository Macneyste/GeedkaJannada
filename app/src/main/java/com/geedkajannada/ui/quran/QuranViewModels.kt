package com.geedkajannada.ui.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.QuranParser
import com.geedkajannada.data.QuranRepository
import com.geedkajannada.data.SettingsStore
import com.geedkajannada.data.Surah
import com.geedkajannada.data.SurahText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuranListUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val query: String = "",
    val surahs: List<Surah> = emptyList(),
    val continueReading: Pair<Surah, Int>? = null,
)

class QuranListViewModel(
    private val repo: QuranRepository,
    store: SettingsStore,
) : ViewModel() {
    private val all = MutableStateFlow<List<Surah>?>(null)
    private val failed = MutableStateFlow(false)
    private val query = MutableStateFlow("")

    init {
        viewModelScope.launch {
            try {
                all.value = repo.surahs()
            } catch (e: Exception) {
                failed.value = true
            }
        }
    }

    val uiState: StateFlow<QuranListUiState> = combine(all, failed, query, store.lastRead) { list, bad, q, last ->
        QuranListUiState(
            loading = list == null && !bad,
            error = bad,
            query = q,
            surahs = list?.let { QuranParser.filter(it, q) }.orEmpty(),
            continueReading = last?.let { (s, v) -> list?.firstOrNull { it.id == s }?.let { it to v } },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuranListUiState())

    fun onQuery(q: String) { query.value = q }
}

data class ReaderUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val text: SurahText? = null,
    val fontSize: Int = 28,
    /** Aayadda la calaamadeeyay ee suuraddan, ama null. */
    val bookmarkedVerse: Int? = null,
    val hasPrev: Boolean = false,
    val hasNext: Boolean = false,
    val startVerse: Int = 1,
)

class ReaderViewModel(
    private val surahId: Int,
    private val startVerseArg: Int,
    private val repo: QuranRepository,
    private val store: SettingsStore,
) : ViewModel() {
    private val text = MutableStateFlow<SurahText?>(null)
    private val failed = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            try {
                val t = repo.surah(surahId)
                if (t == null) failed.value = true else text.value = t
            } catch (e: Exception) {
                failed.value = true
            }
        }
    }

    val uiState: StateFlow<ReaderUiState> =
        combine(text, failed, store.quranFontSize, store.bookmark) { t, bad, size, bm ->
            ReaderUiState(
                loading = t == null && !bad,
                error = bad,
                text = t,
                fontSize = size,
                bookmarkedVerse = bm?.takeIf { it.first == surahId }?.second,
                hasPrev = surahId > 1,
                hasNext = surahId < 114,
                startVerse = startVerseArg,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReaderUiState())

    fun bigger() { viewModelScope.launch { store.setQuranFontSize(uiState.value.fontSize + 2) } }
    fun smaller() { viewModelScope.launch { store.setQuranFontSize(uiState.value.fontSize - 2) } }

    fun onVisibleVerse(verse: Int) {
        viewModelScope.launch { store.setLastRead(surahId, verse) }
    }

    fun toggleBookmark(verse: Int) {
        viewModelScope.launch {
            if (uiState.value.bookmarkedVerse == verse) store.clearBookmark()
            else store.setBookmark(surahId, verse)
        }
    }
}
