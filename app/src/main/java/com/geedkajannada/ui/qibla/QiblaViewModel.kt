package com.geedkajannada.ui.qibla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.City
import com.geedkajannada.data.PrayerRepository
import com.geedkajannada.data.QiblaMath
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class QiblaUiState(val city: City? = null, val bearing: Double = 0.0)

class QiblaViewModel(prayers: PrayerRepository) : ViewModel() {
    val uiState: StateFlow<QiblaUiState> = prayers.city
        .map { QiblaUiState(it, QiblaMath.bearing(it.lat, it.lng)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QiblaUiState())
}
