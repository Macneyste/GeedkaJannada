package com.geedkajannada.ui.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.data.CounterLogic
import com.geedkajannada.data.Dhikr
import com.geedkajannada.data.Dhikrs
import com.geedkajannada.data.SettingsStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CounterUiState(
    val dhikr: Dhikr = Dhikrs.all.first(),
    val count: Int = 0,
    val goal: Int = Dhikrs.all.first().defaultGoal,
    val rounds: Int = 0,
    val soundOn: Boolean = false,
    val hapticsOn: Boolean = false,
    val showGoalReached: Boolean = false,
    val showResetConfirm: Boolean = false,
    val loaded: Boolean = false,
) {
    val ratio: Float get() = CounterLogic.ratio(count, goal)
}

sealed interface CounterEvent {
    /** Hal taabasho oo la aqbalay. */
    data class Tapped(val goalReached: Boolean) : CounterEvent
}

private data class Base(
    val dhikr: Dhikr,
    val count: Int,
    val goal: Int,
    val rounds: Int,
    val sound: Boolean,
    val haptics: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
class CounterViewModel(
    private val store: SettingsStore,
    private val dateKey: () -> String,
    private val nowMs: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private val dialogs = MutableStateFlow(Pair(false, false)) // (goalReached, resetConfirm)
    private var lastTapMs: Long? = null

    private val _events = MutableSharedFlow<CounterEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<CounterEvent> = _events.asSharedFlow()

    private val base = store.selectedDhikrId.flatMapLatest { id ->
        val d = Dhikrs.byId(id)
        combine(
            store.count(d.id), store.goal(d), store.rounds(d.id),
            store.soundEnabled, store.hapticsEnabled,
        ) { c, g, r, s, h -> Base(d, c, g, r, s, h) }
    }

    val uiState: StateFlow<CounterUiState> = combine(base, dialogs) { b, d ->
        CounterUiState(b.dhikr, b.count, b.goal, b.rounds, b.sound, b.haptics, d.first, d.second, true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CounterUiState())

    /** Hal taabasho = hal tiro; taabasho ka yar 120ms waa la diidaa. */
    fun tap() {
        val now = nowMs()
        if (!CounterLogic.acceptTap(now, lastTapMs)) return
        lastTapMs = now
        val s = uiState.value
        if (!s.loaded) return
        viewModelScope.launch {
            val n = store.increment(s.dhikr.id, dateKey())
            val reached = CounterLogic.reachedGoal(n, s.goal)
            if (reached) dialogs.update { it.copy(first = true) }
            _events.emit(CounterEvent.Tapped(reached))
        }
    }

    fun requestReset() = dialogs.update { it.copy(second = true) }
    fun dismissReset() = dialogs.update { it.copy(second = false) }

    fun confirmReset() {
        dismissReset()
        viewModelScope.launch { store.reset(uiState.value.dhikr.id) }
    }

    /** "Sii wad": xir dialog-ka, tirada ha ahaato. */
    fun continueAfterGoal() = dialogs.update { it.copy(first = false) }

    /** "Wareeg cusub": tirada eber, wareegyada +1. */
    fun startNewRound() {
        dialogs.update { it.copy(first = false) }
        viewModelScope.launch { store.newRound(uiState.value.dhikr.id) }
    }
}
