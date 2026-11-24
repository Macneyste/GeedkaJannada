package com.geedkajannada.data

import androidx.annotation.StringRes
import com.geedkajannada.R

enum class Prayer(val key: String, @StringRes val nameRes: Int) {
    SUBAX("subax", R.string.prayer_subax),
    DUHUR("duhur", R.string.prayer_duhur),
    CASAR("casar", R.string.prayer_casar),
    MAGHRIB("maghrib", R.string.prayer_maghrib),
    CISHE("cishe", R.string.prayer_cishe),
}

/** Waqtiga ogeysiinta: 0 = waqtiga salaadda, 5 / 10 daqiiqo ka hor. */
val ReminderOffsets = listOf(0, 5, 10)

data class PrayerEntry(
    val prayer: Prayer,
    /** Daqiiqado min habeenbadh (0..1439), ama null haddii aan la gelin. */
    val minuteOfDay: Int?,
    val notify: Boolean,
)

object PrayerMath {
    private const val DAY_MIN = 24 * 60

    fun format(minuteOfDay: Int): String =
        "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)

    /**
     * Salaadda soo socota ee ka dambeysa waqtiga hadda (ama berri haddii maanta dhammaatay).
     * Waxay soo celisaa (salaad, ilbiriqsiyo ka hadhay).
     */
    fun next(entries: List<PrayerEntry>, nowMinute: Int, nowSecond: Int = 0): Pair<PrayerEntry, Int>? {
        val set = entries.filter { it.minuteOfDay != null }
        if (set.isEmpty()) return null
        val nowTotalSec = nowMinute * 60 + nowSecond
        var best: PrayerEntry? = null
        var bestDelta = Int.MAX_VALUE
        for (e in set) {
            var delta = e.minuteOfDay!! * 60 - nowTotalSec
            if (delta <= 0) delta += DAY_MIN * 60
            if (delta < bestDelta) {
                bestDelta = delta
                best = e
            }
        }
        return best!! to bestDelta
    }

    fun formatRemaining(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }
}
