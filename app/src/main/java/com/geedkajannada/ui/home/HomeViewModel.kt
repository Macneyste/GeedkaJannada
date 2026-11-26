package com.geedkajannada.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.PrayerEntry
import com.geedkajannada.data.PrayerRepository
import com.geedkajannada.data.QuranRepository
import com.geedkajannada.data.SettingsStore
import com.geedkajannada.data.StatsRepository
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
    /** Silsilada maalmaha isku xigta. */
    val streak: Int = 0,
    /** Kaarka "ogeysiisyada way xiran yihiin" waxaa la tusaa marka tani false tahay. */
    val notificationsOn: Boolean = true,
)

class HomeViewModel(
    prayers: PrayerRepository,
    store: SettingsStore,
    quran: QuranRepository,
    stats: StatsRepository,
) : ViewModel() {
    private val surahs = MutableStateFlow<List<Surah>>(emptyList())

    init {
        viewModelScope.launch { runCatching { surahs.value = quran.surahs() } }
    }

    val uiState: StateFlow<HomeUiState> =
        combine(prayers.entries, store.lastRead, surahs, stats.summary, store.notificationsEnabled) { e, last, list, s, on ->
            HomeUiState(e, last?.let { (id, verse) -> list.firstOrNull { it.id == id }?.let { it to verse } }, s.streak, on)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
