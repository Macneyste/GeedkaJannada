package com.geedkajannada.ui.adhkar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.AdhkarCatalog
import com.geedkajannada.data.AdhkarItem
import com.geedkajannada.data.AdhkarPeriod
import com.geedkajannada.data.AdhkarSource
import com.geedkajannada.data.QuranRepository
import com.geedkajannada.data.SettingsStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdhkarRow(val item: AdhkarItem, val arabic: String, val done: Boolean)

data class AdhkarUiState(
    val period: AdhkarPeriod = AdhkarPeriod.SUBAX,
    val rows: List<AdhkarRow> = emptyList(),
    val error: Boolean = false,
) {
    val doneCount: Int get() = rows.count { it.done }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AdhkarViewModel(
    private val quran: QuranRepository,
    private val store: SettingsStore,
    private val dateKey: () -> String,
    initialPeriod: AdhkarPeriod,
) : ViewModel() {
    private val period = MutableStateFlow(initialPeriod)
    private val texts = MutableStateFlow<Map<String, String>?>(null)
    private val failed = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            try {
                texts.value = loadTexts()
            } catch (e: Exception) {
                failed.value = true
            }
        }
    }

    private suspend fun loadTexts(): Map<String, String> = AdhkarCatalog.items.associate { item ->
        item.id to when (val s = item.source) {
            is AdhkarSource.Text -> s.arabic
            is AdhkarSource.QuranRange -> {
                val verses = quran.surah(s.surah)?.verses ?: error("surah ${s.surah}")
                verses.subList(s.from - 1, s.to).joinToString(" ")
            }
        }
    }

    val uiState: StateFlow<AdhkarUiState> = period.flatMapLatest { p ->
        combine(store.adhkarDone(dateKey(), p), texts, failed) { done, t, bad ->
            AdhkarUiState(
                period = p,
                rows = if (t == null) emptyList()
                else AdhkarCatalog.forPeriod(p).map { AdhkarRow(it, t.getValue(it.id), it.id in done) },
                error = bad,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdhkarUiState(initialPeriod))

    fun selectPeriod(p: AdhkarPeriod) { period.value = p }

    fun toggle(item: AdhkarItem, done: Boolean) {
        viewModelScope.launch { store.setAdhkarDone(dateKey(), period.value, item.id, done) }
    }
}
