package com.geedkajannada.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.StatsRepository
import com.geedkajannada.data.StatsSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class HistoryViewModel(repo: StatsRepository) : ViewModel() {
    val summary: StateFlow<StatsSummary?> =
        repo.summary.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @Suppress("unused")
    val today: LocalDate get() = LocalDate.now()
}
