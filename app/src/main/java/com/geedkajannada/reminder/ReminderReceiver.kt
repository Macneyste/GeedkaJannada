package com.geedkajannada.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.geedkajannada.container
import com.geedkajannada.data.Prayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Alarm-ka marka uu dhaco: tus ogeysiinta, kadibna dib u deji berrito. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = Prayer.entries.firstOrNull { it.key == intent.getStringExtra(ReminderScheduler.EXTRA_PRAYER) }
            ?: return
        val offset = intent.getIntExtra(ReminderScheduler.EXTRA_OFFSET, 0)
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                showFromStore(context, prayer, offset)
                ReminderScheduler.rescheduleAll(context, context.container.settings)
            } finally {
                pending.finish()
            }
        }
    }
}

/** Qalabka marka dib loo bilaabo, alarm-yada dib u deji. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
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

/** Akhri waqtiga, goobta iyo habka dhawaaqa, kadibna tus ogeysiinta. */
internal suspend fun showFromStore(context: Context, prayer: Prayer, earlyMinutes: Int) {
    val store = context.container.settings
    if (!store.notificationsEnabled.first()) return
    val minute = store.prayerEntries.first().firstOrNull { it.prayer == prayer }?.minuteOfDay
    NotificationHelper.showPrayer(
        context, prayer, earlyMinutes, minute, store.prayerCity.first().nameSo, store.notifyMode.first(),
    )
}
