package com.geedkajannada.data

import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DayBar(val date: LocalDate, val total: Int)

data class StatsSummary(
    val today: Int,
    val week: Int,
    val streak: Int,
    val last7: List<DayBar>,
    val perDhikr: Map<String, Int>,
)

object StatsCalc {
    fun dateKey(date: LocalDate): String = date.toString() // yyyy-MM-dd

    fun summarize(daily: Map<String, Map<String, Int>>, today: LocalDate): StatsSummary {
        fun total(d: LocalDate) = daily[dateKey(d)]?.values?.sum() ?: 0
        val last7 = (6 downTo 0).map { DayBar(today.minusDays(it.toLong()), total(today.minusDays(it.toLong()))) }
        // Silsilad: maalmo isku xigta oo tiro leh; haddii maanta eber tahay, laga bilaabo shalay.
        var cursor = if (total(today) > 0) today else today.minusDays(1)
        var streak = 0
        while (total(cursor) > 0) {
            streak++
            cursor = cursor.minusDays(1)
        }
        val perDhikr = HashMap<String, Int>()
        for ((_, m) in daily) for ((id, n) in m) perDhikr[id] = (perDhikr[id] ?: 0) + n
        return StatsSummary(total(today), last7.sumOf { it.total }, streak, last7, perDhikr)
    }
}

class StatsRepository(
    private val store: SettingsStore,
    private val zone: () -> ZoneId = { ZoneId.systemDefault() },
) {
    fun todayKey(): String = StatsCalc.dateKey(LocalDate.now(zone()))

    val summary: Flow<StatsSummary> = store.dailyTotals.map { StatsCalc.summarize(it, LocalDate.now(zone())) }
}
