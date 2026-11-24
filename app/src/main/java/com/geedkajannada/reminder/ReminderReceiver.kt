package com.geedkajannada.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.geedkajannada.container
import com.geedkajannada.data.Prayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Alarm-ka marka uu dhaco: tus ogeysiinta, kadibna dib u deji berrito. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = Prayer.entries.firstOrNull { it.key == intent.getStringExtra(ReminderScheduler.EXTRA_PRAYER) }
            ?: return
        val offset = intent.getIntExtra(ReminderScheduler.EXTRA_OFFSET, 0)
        NotificationHelper.showPrayer(context, prayer, offset)
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
