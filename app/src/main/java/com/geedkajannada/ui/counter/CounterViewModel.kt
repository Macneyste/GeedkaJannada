package com.geedkajannada.ui.counter

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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Hal dhalo: dikhriga, tiradiisa iyo yoolkiisa. */
data class Jar(val dhikr: Dhikr, val count: Int, val goal: Int) {
    val full: Boolean get() = count >= goal
}

data class CounterUiState(
    val jars: List<Jar> = Dhikrs.all.map { Jar(it, 0, it.defaultGoal) },
    /** Dikhriga hadda la tirinayo (0..2). */
    val index: Int = 0,
    val rounds: Int = 0,
    val soundOn: Boolean = false,
    val hapticsOn: Boolean = false,
    val showResetConfirm: Boolean = false,
    val loaded: Boolean = false,
) {
    val current: Jar get() = jars[index]
    val total: Int get() = jars.sumOf { minOf(it.count, it.goal) }
    val totalGoal: Int get() = jars.sumOf { it.goal }
    val complete: Boolean get() = jars.all { it.full }
}

sealed interface CounterEvent {
    /** Hal taabasho oo la aqbalay. */
    data class Tapped(val jarFilled: Boolean, val completed: Boolean) : CounterEvent
}

class CounterViewModel(
    private val store: SettingsStore,
    private val dateKey: () -> String,
    private val nowMs: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private val resetConfirm = MutableStateFlow(false)
    private var lastTapMs: Long? = null
    private var tapping = false

    private val _events = MutableSharedFlow<CounterEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<CounterEvent> = _events.asSharedFlow()

    private val jars = combine(
        Dhikrs.all.map { d -> combine(store.count(d.id), store.goal(d)) { c, g -> Jar(d, c, g) } },
    ) { it.toList() }

    private val prefs = combine(
        store.selectedDhikrId, store.rounds(Dhikrs.all.first().id), store.soundEnabled, store.hapticsEnabled,
    ) { id, rounds, sound, haptics -> Prefs(id, rounds, sound, haptics) }

    private data class Prefs(val selected: String, val rounds: Int, val sound: Boolean, val haptics: Boolean)

    val uiState: StateFlow<CounterUiState> = combine(jars, prefs, resetConfirm) { j, p, confirm ->
        CounterUiState(
            jars = j,
            index = j.indexOfFirst { it.dhikr.id == p.selected }.coerceAtLeast(0),
            rounds = p.rounds,
            soundOn = p.sound,
            hapticsOn = p.haptics,
            showResetConfirm = confirm,
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CounterUiState())

    /** Hal taabasho = hal tiro; taabasho ka yar 120ms waa la diidaa. Marka dhaladu buuxsanto, tan xigta ayaa loo gudbaa. */
    fun tap() {
        val now = nowMs()
        if (!CounterLogic.acceptTap(now, lastTapMs)) return
        val s = uiState.value
        if (!s.loaded || s.complete || tapping) return
        lastTapMs = now
        tapping = true
        viewModelScope.launch {
            try {
                val target = if (!s.current.full) s.current else s.jars.first { !it.full }
                val n = store.increment(target.dhikr.id, dateKey())
                val filled = n >= target.goal
                val next = if (filled) nextOpen(s.jars, target) else null
                store.selectDhikr((next ?: target).dhikr.id)
                _events.emit(CounterEvent.Tapped(jarFilled = filled, completed = filled && next == null))
            } finally {
                tapping = false
            }
        }
    }

    /** Dhalada xigta ee aan weli buuxsamin (ka dib `done`, kadib dib u soo laabo). */
    private fun nextOpen(all: List<Jar>, done: Jar): Jar? {
        val i = all.indexOfFirst { it.dhikr.id == done.dhikr.id }
        return (1 until all.size).map { all[(i + it) % all.size] }.firstOrNull { !it.full }
    }

    fun requestReset() { resetConfirm.value = true }
    fun dismissReset() { resetConfirm.value = false }

    /** "Dib u bilow": eber saddexda dhalo. */
    fun confirmReset() {
        dismissReset()
        viewModelScope.launch {
            Dhikrs.all.forEach { store.reset(it.id) }
            store.selectDhikr(Dhikrs.all.first().id)
        }
    }

    /** "Wareeg cusub": ka dib 99, eber oo wareegyada +1. */
    fun startNewRound() {
        viewModelScope.launch {
            Dhikrs.all.forEach { store.newRound(it.id) }
            store.selectDhikr(Dhikrs.all.first().id)
        }
    }
}
