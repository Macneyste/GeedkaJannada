package com.geedkajannada.ui.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.Cities
import com.geedkajannada.data.City
import com.geedkajannada.data.NotifyMode
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
    val auto: Boolean = true,
    val city: City = Cities.default,
    /** Isticmaaluhu wuu diiday ogeysiinta: tus sharaxaad (ma jiro badhan been ah). */
    val notificationsDenied: Boolean = false,
    val enabled: Boolean = false,
    val mode: NotifyMode = NotifyMode.SOUND,
)

class PrayerViewModel(
    private val repo: PrayerRepository,
    private val reschedule: suspend () -> Unit,
) : ViewModel() {
    private val denied = MutableStateFlow(false)

    val uiState: StateFlow<PrayerUiState> =
        combine(repo.entries, repo.offset, repo.auto, repo.city, denied) { e, o, a, c, d -> PrayerUiState(e, o, a, c, d) }
            .combine(repo.notificationsEnabled) { s, on -> s.copy(enabled = on) }
            .combine(repo.notifyMode) { s, m -> s.copy(mode = m) }
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

    fun setAuto(on: Boolean) {
        viewModelScope.launch {
            repo.setAuto(on)
            reschedule()
        }
    }

    fun setCity(id: String) {
        viewModelScope.launch {
            repo.setCity(id)
            reschedule()
        }
    }

    fun setOffset(minutes: Int) {
        viewModelScope.launch {
            repo.setOffset(minutes)
            reschedule()
        }
    }

    fun setEnabled(on: Boolean) {
        viewModelScope.launch {
            repo.setNotificationsEnabled(on)
            reschedule()
        }
    }

    fun setMode(mode: NotifyMode) {
        viewModelScope.launch { repo.setNotifyMode(mode) }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            denied.value = false
            setEnabled(true)
        } else {
            denied.update { true }
        }
    }

    fun dismissDenied() { denied.value = false }
}
