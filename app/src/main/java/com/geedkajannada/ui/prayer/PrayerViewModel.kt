package com.geedkajannada.ui.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.Prayer
import com.geedkajannada.data.PrayerEntry
import com.geedkajannada.data.PrayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrayerUiState(
    val entries: List<PrayerEntry> = emptyList(),
    val offset: Int = 0,
    /** Isticmaaluhu wuu diiday ogeysiinta: tus sharaxaad (ma jiro badhan been ah). */
    val notificationsDenied: Boolean = false,
)

class PrayerViewModel(
    private val repo: PrayerRepository,
    private val reschedule: suspend () -> Unit,
) : ViewModel() {
    private val denied = MutableStateFlow(false)

    val uiState: StateFlow<PrayerUiState> =
        combine(repo.entries, repo.offset, denied) { e, o, d -> PrayerUiState(e, o, d) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrayerUiState())

    fun setTime(prayer: Prayer, minuteOfDay: Int) {
        viewModelScope.launch {
            repo.setTime(prayer, minuteOfDay)
            reschedule()
        }
    }

    fun setNotify(prayer: Prayer, on: Boolean) {
        viewModelScope.launch {
            repo.setNotify(prayer, on)
            reschedule()
        }
    }

    fun setOffset(minutes: Int) {
        viewModelScope.launch {
            repo.setOffset(minutes)
            reschedule()
        }
    }

    fun onPermissionResult(granted: Boolean, prayer: Prayer) {
        if (granted) {
            denied.value = false
            setNotify(prayer, true)
        } else {
            denied.update { true }
        }
    }

    fun dismissDenied() { denied.value = false }
}
