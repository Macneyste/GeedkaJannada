package com.geedkajannada.reminder

import com.geedkajannada.data.Prayer
import com.geedkajannada.data.PrayerEntry
import java.time.LocalDate
import java.time.ZonedDateTime

/** Hal alarm oo la jadwalayo. */
data class PlannedAlarm(
    val prayer: Prayer,
    val atMillis: Long,
    /** Waqtiga salaadda lafteeda. */
    val prayerMillis: Long,
    /** 0 = waqtiga salaadda; > 0 = ogeysiis hore. */
    val earlyMinutes: Int,
    val requestCode: Int,
)

/** Mantiq saafi ah (la tijaabin karo) oo go'aamiya alarm-yada. */
object ReminderPlan {
    const val DAYS = 7
    private val PER_DAY = Prayer.entries.size * 2
    val REQUEST_CODES: IntRange = 0 until DAYS * PER_DAY

    /** Ogeysiis ka daaha wax ka badan intan lama tuso. */
    const val STALE_AFTER_MS = 10 * 60 * 1000L

    fun plan(
        days: List<Pair<LocalDate, List<PrayerEntry>>>,
        earlyMinutes: Int,
        now: ZonedDateTime,
    ): List<PlannedAlarm> {
        val out = mutableListOf<PlannedAlarm>()
        days.take(DAYS).forEachIndexed { dayIndex, (date, entries) ->
            for (e in entries) {
                val minute = e.minuteOfDay ?: continue
                if (!e.notify) continue
                val prayerAt = date.atTime(minute / 60, minute % 60).atZone(now.zone)
                val base = dayIndex * PER_DAY + e.prayer.ordinal * 2
                fun add(at: ZonedDateTime, early: Int, code: Int) {
                    // Waqti dhaafay lama jadwaleeyo.
                    if (at.isAfter(now)) {
                        out += PlannedAlarm(e.prayer, at.toInstant().toEpochMilli(), prayerAt.toInstant().toEpochMilli(), early, code)
                    }
                }
                add(prayerAt, 0, base)
                if (earlyMinutes > 0) add(prayerAt.minusMinutes(earlyMinutes.toLong()), earlyMinutes, base + 1)
            }
        }
        return out
    }

    /** True haddii ogeysiintu duugowday: aad u daahday, ama ogeysiis hore oo salaaddu horay u soo gashay. */
    fun isStale(nowMillis: Long, atMillis: Long, prayerMillis: Long, earlyMinutes: Int): Boolean =
        nowMillis - atMillis > STALE_AFTER_MS || (earlyMinutes > 0 && nowMillis >= prayerMillis)
}
