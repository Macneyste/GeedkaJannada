package com.geedkajannada.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.LocationProvider
import com.geedkajannada.data.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LocationUiState(
    /** Weydii isticmaaluhu markii ugu horreysay (dialog sharax leh). */
    val showPrompt: Boolean = false,
    val busy: Boolean = false,
    /** Goobta lama helin (location la xidhay, ama permission la diiday). */
    val failed: Boolean = false,
    val hasLocation: Boolean = false,
)

class LocationViewModel(
    private val store: SettingsStore,
    private val provider: LocationProvider,
    private val reschedule: suspend () -> Unit,
) : ViewModel() {
    private val busy = MutableStateFlow(false)
    private val failed = MutableStateFlow(false)

    val uiState: StateFlow<LocationUiState> =
        combine(store.locationAsked, store.savedLocation, busy, failed) { asked, loc, b, f ->
            LocationUiState(showPrompt = !asked, busy = b, failed = f, hasLocation = loc != null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LocationUiState())

    /** Isticmaaluhu wuu diiday ama ka gudbay dialog-ga: ha dib u weydiin si toos ah. */
    fun dismissPrompt() {
        viewModelScope.launch { store.setLocationAsked() }
    }

    /** Natiijada codsiga permission-ka. */
    fun onPermissionResult(granted: Boolean) {
        viewModelScope.launch { store.setLocationAsked() }
        if (granted) refresh() else failed.value = true
    }

    /** Hel goobta hadda oo cusboonaysii waqtiyada salaadda iyo xusuusinta. */
    fun refresh() {
        if (busy.value) return
        viewModelScope.launch {
            busy.value = true
            failed.value = false
            val loc = runCatching { provider.current() }.getOrNull()
            if (loc != null) {
                store.setLocation(loc.first, loc.second)
                reschedule()
            } else {
                failed.value = true
            }
            busy.value = false
        }
    }

    fun clear() {
        viewModelScope.launch {
            store.clearLocation()
            reschedule()
        }
    }
}
