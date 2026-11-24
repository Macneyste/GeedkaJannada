package com.geedkajannada.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.geedkajannada.data.Prayer
import com.geedkajannada.data.PrayerTimesCalc
import com.geedkajannada.data.SettingsStore
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

object ReminderScheduler {
    const val EXTRA_PRAYER = "prayer"
    const val EXTRA_OFFSET = "offset"

    /** Waqtiga xiga ee ogeysiinta: haddii hadda ka dhaafay, berri. */
    fun nextTrigger(minuteOfDay: Int, offsetMinutes: Int, now: ZonedDateTime): ZonedDateTime {
        var t = now.toLocalDate().atStartOfDay(now.zone).plusMinutes((minuteOfDay - offsetMinutes).toLong())
        if (!t.isAfter(now)) t = t.plusDays(1)
        return t
    }

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    }

    private fun pendingIntent(context: Context, prayer: Prayer, offset: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(EXTRA_PRAYER, prayer.key)
            .putExtra(EXTRA_OFFSET, offset)
        return PendingIntent.getBroadcast(
            context, prayer.ordinal, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun workName(prayer: Prayer) = "reminder_${prayer.key}"

    fun cancel(context: Context, prayer: Prayer) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context, prayer, 0))
        WorkManager.getInstance(context).cancelUniqueWork(workName(prayer))
    }

    private fun schedule(context: Context, prayer: Prayer, at: ZonedDateTime, offset: Int) {
        cancel(context, prayer)
        val triggerMs = at.toInstant().toEpochMilli()
        if (canScheduleExact(context)) {
            context.getSystemService(AlarmManager::class.java)
                .setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent(context, prayer, offset))
        } else {
            // Exact alarm lama oggola: WorkManager oo inexact ah.
            val delay = (triggerMs - System.currentTimeMillis()).coerceAtLeast(0)
            val req = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(EXTRA_PRAYER to prayer.key, EXTRA_OFFSET to offset))
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(workName(prayer), ExistingWorkPolicy.REPLACE, req)
        }
    }

    /** Dib u deji dhammaan ogeysiisyada la furay (ka dib bedelid, fire, ama boot). */
    suspend fun rescheduleAll(context: Context, store: SettingsStore) {
        val offset = store.reminderOffset.first()
        val now = ZonedDateTime.now(PrayerTimesCalc.ZONE)
        val today = now.toLocalDate()
        val todayEntries = store.prayerEntriesFor(today)
        val tomorrowEntries = store.prayerEntriesFor(today.plusDays(1))
        for (e in todayEntries) {
            val tomorrow = tomorrowEntries.first { it.prayer == e.prayer }
            if (!e.notify || e.minuteOfDay == null || tomorrow.minuteOfDay == null) {
                cancel(context, e.prayer)
                continue
            }
            // Maanta haddii weli soo socoto, haddii kale berri (waqtiga berrito si sax ah loo xisaabiyay).
            val todayAt = today.atStartOfDay(now.zone).plusMinutes((e.minuteOfDay - offset).toLong())
            val at = if (todayAt.isAfter(now)) todayAt
            else today.plusDays(1).atStartOfDay(now.zone).plusMinutes((tomorrow.minuteOfDay - offset).toLong())
            schedule(context, e.prayer, at, offset)
        }
    }
}
