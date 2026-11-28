package com.geedkajannada.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.geedkajannada.container
import com.geedkajannada.data.Prayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Alarm-ka marka uu dhaco: tus ogeysiinta (haddii aanay duugoobin), kadibna buuxi jadwalka. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = Prayer.entries.firstOrNull { it.key == intent.getStringExtra(ReminderScheduler.EXTRA_PRAYER) }
            ?: return
        val early = intent.getIntExtra(ReminderScheduler.EXTRA_EARLY, 0)
        val now = System.currentTimeMillis()
        val stale = ReminderPlan.isStale(
            now,
            intent.getLongExtra(ReminderScheduler.EXTRA_AT, now),
            intent.getLongExtra(ReminderScheduler.EXTRA_PRAYER_AT, now),
            early,
        )
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (!stale) PrayerAlert.fire(context, prayer, early)
                ReminderScheduler.rescheduleAll(context, context.container.settings)
            } finally {
                pending.finish()
            }
        }
    }
}

/** Boot, cusboonaysiin app, beddel saacad/timezone ama ogolaansho alarm: dib u jadwali. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in RESCHEDULE_ACTIONS) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                ReminderScheduler.rescheduleAll(context, context.container.settings)
            } finally {
                pending.finish()
            }
        }
    }
}

private val RESCHEDULE_ACTIONS = setOf(
    Intent.ACTION_BOOT_COMPLETED,
    Intent.ACTION_MY_PACKAGE_REPLACED,
    Intent.ACTION_TIME_CHANGED,
    Intent.ACTION_TIMEZONE_CHANGED,
    "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED",
)
