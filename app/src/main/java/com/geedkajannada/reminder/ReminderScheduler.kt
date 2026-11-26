package com.geedkajannada.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.geedkajannada.data.PrayerTimesCalc
import com.geedkajannada.data.SettingsStore
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ReminderScheduler {
    const val EXTRA_PRAYER = "prayer"
    const val EXTRA_EARLY = "early"
    const val EXTRA_AT = "at"
    const val EXTRA_PRAYER_AT = "prayer_at"

    private val lock = Mutex()

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    }

    private fun intent(context: Context) = Intent(context, ReminderReceiver::class.java)

    private fun cancelAll(context: Context, alarms: AlarmManager) {
        for (code in ReminderPlan.REQUEST_CODES) {
            val existing = PendingIntent.getBroadcast(
                context, code, intent(context), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
            ) ?: continue
            alarms.cancel(existing)
            existing.cancel()
        }
    }

    private fun schedule(context: Context, alarms: AlarmManager, a: PlannedAlarm, exact: Boolean) {
        val pi = PendingIntent.getBroadcast(
            context, a.requestCode,
            intent(context)
                .putExtra(EXTRA_PRAYER, a.prayer.key)
                .putExtra(EXTRA_EARLY, a.earlyMinutes)
                .putExtra(EXTRA_AT, a.atMillis)
                .putExtra(EXTRA_PRAYER_AT, a.prayerMillis),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        if (exact) {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, a.atMillis, pi)
        } else {
            // Exact alarm lama oggola: waa la soo daahin karaa (waxaa lagu sheegaa shaashadda Salaad).
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, a.atMillis, pi)
        }
    }

    /** Tirtir dhammaan, kadibna jadwali 7-da maalmood ee soo socda. */
    suspend fun rescheduleAll(context: Context, store: SettingsStore) = lock.withLock {
        val alarms = context.getSystemService(AlarmManager::class.java)
        cancelAll(context, alarms)
        if (!store.notificationsEnabled.first()) return@withLock
        val now = ZonedDateTime.now(PrayerTimesCalc.ZONE)
        val days = (0 until ReminderPlan.DAYS).map {
            val date = now.toLocalDate().plusDays(it.toLong())
            date to store.prayerEntriesFor(date)
        }
        val exact = canScheduleExact(context)
        ReminderPlan.plan(days, store.reminderOffset.first(), now).forEach { schedule(context, alarms, it, exact) }
    }
}
