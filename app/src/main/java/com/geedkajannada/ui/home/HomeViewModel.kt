package com.geedkajannada.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.AdhkarCatalog
import com.geedkajannada.data.AdhkarPeriod
import com.geedkajannada.data.Prayer
import com.geedkajannada.data.PrayerEntry
import com.geedkajannada.data.PrayerRepository
import com.geedkajannada.data.QuranRepository
import com.geedkajannada.data.SettingsStore
import com.geedkajannada.data.StatsCalc
import com.geedkajannada.data.StatsRepository
import com.geedkajannada.data.Surah
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
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
    /** Salaadaha maanta la calaamadeeyay. */
    val checked: Set<Prayer> = emptySet(),
    /** Silsilada maalmaha isku xigta. */
    val streak: Int = 0,
    /** Adkaarta subaxa ee maanta la dhammeeyay / wadarta. */
    val subaxDone: Int = 0,
    val subaxTotal: Int = AdhkarCatalog.forPeriod(AdhkarPeriod.SUBAX).size,
)

class HomeViewModel(
    prayers: PrayerRepository,
    private val store: SettingsStore,
    quran: QuranRepository,
    stats: StatsRepository,
    private val today: () -> String = { StatsCalc.dateKey(LocalDate.now()) },
) : ViewModel() {
    private val surahs = MutableStateFlow<List<Surah>>(emptyList())

    init {
        viewModelScope.launch { runCatching { surahs.value = quran.surahs() } }
    }

    private val base: Flow<HomeUiState> =
        combine(prayers.entries, store.lastRead, surahs, store.checkedPrayers(today())) { e, last, list, checked ->
            HomeUiState(e, last?.let { (s, v) -> list.firstOrNull { it.id == s }?.let { it to v } }, checked)
        }

    val uiState: StateFlow<HomeUiState> =
        combine(base, stats.summary, store.adhkarDone(today(), AdhkarPeriod.SUBAX)) { b, s, done ->
            b.copy(streak = s.streak, subaxDone = done.size)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun toggleChecked(prayer: Prayer) {
        viewModelScope.launch { store.toggleChecked(today(), prayer) }
    }
}
