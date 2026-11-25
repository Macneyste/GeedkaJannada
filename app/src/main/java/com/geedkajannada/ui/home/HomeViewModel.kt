package com.geedkajannada.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.PrayerEntry
import com.geedkajannada.data.PrayerRepository
import com.geedkajannada.data.QuranRepository
import com.geedkajannada.data.SettingsStore
import com.geedkajannada.data.Surah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val entries: List<PrayerEntry> = emptyList(),
    /** Suuradda iyo aayadda ugu dambaysay la akhriay, ama null. */
    val lastRead: Pair<Surah, Int>? = null,
)

class HomeViewModel(
    prayers: PrayerRepository,
    store: SettingsStore,
    quran: QuranRepository,
) : ViewModel() {
    private val surahs = MutableStateFlow<List<Surah>>(emptyList())

    init {
        viewModelScope.launch { runCatching { surahs.value = quran.surahs() } }
    }

    val uiState: StateFlow<HomeUiState> =
        combine(prayers.entries, store.lastRead, surahs) { e, last, list ->
            HomeUiState(e, last?.let { (s, v) -> list.firstOrNull { it.id == s }?.let { it to v } })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
