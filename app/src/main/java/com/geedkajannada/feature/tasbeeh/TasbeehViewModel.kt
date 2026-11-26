package com.geedkajannada.feature.tasbeeh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.CounterLogic
import com.geedkajannada.data.Dhikr
import com.geedkajannada.data.Dhikrs
import com.geedkajannada.data.SettingsStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Hal dhalo: dhikriga iyo tiradiisa. */
data class Jar(val dhikr: Dhikr, val count: Int) {
    val goal: Int get() = dhikr.defaultGoal
    val full: Boolean get() = count >= goal
}

data class TasbeehUiState(
    val jars: List<Jar> = Dhikrs.all.map { Jar(it, 0) },
    val index: Int = 0,
    val totalCompleted: Int = 0,
    val soundOn: Boolean = true,
    val showResetConfirm: Boolean = false,
    val loaded: Boolean = false,
) {
    val current: Jar get() = jars[index]
    val total: Int get() = jars.sumOf { it.count }
    val totalGoal: Int get() = jars.sumOf { it.goal }
    val complete: Boolean get() = jars.all { it.full }
}

enum class TasbeehSound { TAP, BEAD, CHIME, NEXT, COMPLETE }

sealed interface TasbeehEvent {
    /** Dhawaaq la ciyaarayo `delayMs` ka dib. Lama soo diro marka muqalku damsan yahay. */
    data class Sound(val sound: TasbeehSound, val delayMs: Long = 0) : TasbeehEvent
    data class Haptic(val strong: Boolean) : TasbeehEvent
    data object Complete : TasbeehEvent
}

class TasbeehViewModel(
    private val repo: TasbeehRepository,
    private val settings: SettingsStore,
    private val dateKey: () -> String,
    private val nowMs: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private val resetConfirm = MutableStateFlow(false)
    private var lastTapMs: Long? = null

    private val _events = MutableSharedFlow<TasbeehEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<TasbeehEvent> = _events.asSharedFlow()

    val uiState: StateFlow<TasbeehUiState> =
        combine(repo.data, settings.soundEnabled, resetConfirm) { data, sound, confirm ->
            TasbeehUiState(
                jars = Dhikrs.all.mapIndexed { i, d -> Jar(d, data.counts[i]) },
                index = data.index,
                totalCompleted = data.totalCompleted,
                soundOn = sound,
                showResetConfirm = confirm,
                loaded = true,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasbeehUiState())

    /** Hal taabasho = hal tiro; taabasho ka yar 120ms waa la diidaa. */
    fun tap() {
        val now = nowMs()
        if (!CounterLogic.acceptTap(now, lastTapMs)) return
        lastTapMs = now
        viewModelScope.launch {
            val outcome = repo.tap(dateKey()) ?: return@launch
            _events.emit(TasbeehEvent.Haptic(strong = outcome.jarFilled))
            val sounds = when {
                outcome.completed -> listOf(TasbeehEvent.Sound(TasbeehSound.COMPLETE))
                outcome.jarFilled -> listOf(
                    TasbeehEvent.Sound(TasbeehSound.CHIME),
                    TasbeehEvent.Sound(TasbeehSound.NEXT, delayMs = 450),
                )
                else -> listOf(
                    TasbeehEvent.Sound(TasbeehSound.TAP),
                    TasbeehEvent.Sound(TasbeehSound.BEAD, delayMs = 220),
                )
            }
            play(sounds)
            if (outcome.completed) _events.emit(TasbeehEvent.Complete)
        }
    }

    /** "Xiga ›" */
    fun next() {
        viewModelScope.launch {
            if (repo.skipToNext()) play(listOf(TasbeehEvent.Sound(TasbeehSound.NEXT)))
        }
    }

    /** Haddii muqalku damsan yahay, wax dhawaaq ah lama diro. */
    private suspend fun play(sounds: List<TasbeehEvent.Sound>) {
        if (uiState.value.soundOn) sounds.forEach { _events.emit(it) }
    }

    fun toggleSound() {
        viewModelScope.launch { settings.setSound(!settings.soundEnabled.first()) }
    }

    fun requestReset() { resetConfirm.value = true }
    fun dismissReset() { resetConfirm.value = false }

    fun confirmReset() {
        dismissReset()
        viewModelScope.launch { repo.reset() }
    }

    /** Badhanka "Dib u bilow" ee kaarka dhammaystirka: xaqiijin looma baahna, wax tiro ah lama lumiyo. */
    fun restartAfterComplete() {
        viewModelScope.launch { repo.reset() }
    }
}
