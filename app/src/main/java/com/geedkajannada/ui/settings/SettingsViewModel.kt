package com.geedkajannada.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.SettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(val soundOn: Boolean = false, val hapticsOn: Boolean = false)

class SettingsViewModel(private val store: SettingsStore) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> =
        combine(store.soundEnabled, store.hapticsEnabled) { s, h -> SettingsUiState(s, h) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setSound(on: Boolean) { viewModelScope.launch { store.setSound(on) } }
    fun setHaptics(on: Boolean) { viewModelScope.launch { store.setHaptics(on) } }
}
