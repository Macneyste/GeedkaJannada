package com.geedkajannada.feature.tasbeeh

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.geedkajannada.data.Dhikr
import com.geedkajannada.data.Dhikrs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Xaaladda Tasbeeh ee la kaydiyay. */
data class TasbeehData(
    val index: Int,
    /** Tirada dhikri kasta, isla tartanka Dhikrs.all (0..33). */
    val counts: List<Int>,
    /** Immisa jeer ayaa silsiladda oo dhan (99) la dhammeeyay. */
    val totalCompleted: Int,
)

/** Natiijada hal taabasho oo la tiriyay. */
data class TapOutcome(val jarFilled: Boolean, val completed: Boolean)

/**
 * Kaydinta Tasbeeh (DataStore). Furayaasha tirada iyo wadarta maalinta waa isla kuwii hore,
 * si xogtii hore iyo Taariikhda ay u sii shaqeeyaan.
 */
class TasbeehRepository(private val dataStore: DataStore<Preferences>) {

    private val selected = stringPreferencesKey("selected_dhikr")
    private val completedKey = intPreferencesKey("tas_total_completed")
    private fun countKey(d: Dhikr) = intPreferencesKey("count_${d.id}")
    private fun dayKey(date: String, d: Dhikr) = intPreferencesKey("day_${date}_${d.id}")

    val data: Flow<TasbeehData> = dataStore.data.map { read(it) }

    private fun read(p: Preferences) = TasbeehData(
        index = Dhikrs.all.indexOfFirst { it.id == p[selected] }.coerceAtLeast(0),
        counts = Dhikrs.all.map { (p[countKey(it)] ?: 0).coerceIn(0, it.defaultGoal) },
        totalCompleted = p[completedKey] ?: 0,
    )

    private fun isFull(counts: List<Int>, i: Int) = counts[i] >= Dhikrs.all[i].defaultGoal

    /** Dhalada xigta ee aan buuxin, ka dib `from` (wareeg). */
    private fun nextOpen(counts: List<Int>, from: Int): Int? =
        (1..counts.size).map { (from + it) % counts.size }.firstOrNull { !isFull(counts, it) }

    private fun MutablePreferences.select(i: Int) {
        this[selected] = Dhikrs.all[i].id
    }

    /**
     * Hal tiro, hal edit oo atomic ah. Marka 33 la gaaro si toos ah ayaa loogu gudbaa dhikriga xiga.
     * Waxay soo celisaa null haddii wareeggu horay u dhammaaday.
     */
    suspend fun tap(dateKey: String): TapOutcome? {
        var outcome: TapOutcome? = null
        dataStore.edit { p ->
            val s = read(p)
            val i = if (!isFull(s.counts, s.index)) s.index else nextOpen(s.counts, s.index) ?: return@edit
            val d = Dhikrs.all[i]
            val counts = s.counts.toMutableList().also { it[i] = it[i] + 1 }
            p[countKey(d)] = counts[i]
            p[dayKey(dateKey, d)] = (p[dayKey(dateKey, d)] ?: 0) + 1

            val filled = isFull(counts, i)
            val next = if (filled) nextOpen(counts, i) else null
            val completed = filled && next == null
            if (completed) p[completedKey] = s.totalCompleted + 1
            p.select(next ?: i)
            outcome = TapOutcome(filled, completed)
        }
        return outcome
    }

    /** "Xiga ›": u gudub dhikriga xiga ee aan buuxin; tirooyinka lama beddelo. Soo celi true haddii la gudbay. */
    suspend fun skipToNext(): Boolean {
        var moved = false
        dataStore.edit { p ->
            val s = read(p)
            val next = nextOpen(s.counts, s.index)
            if (next != null && next != s.index) {
                p.select(next)
                moved = true
            }
        }
        return moved
    }

    /** "Dib u bilow": nadiifi saddexda dhalo. Wadarta dhammaystirka iyo Taariikhda waa la hayaa. */
    suspend fun reset() {
        dataStore.edit { p ->
            Dhikrs.all.forEach { p[countKey(it)] = 0 }
            p.select(0)
        }
    }
}
