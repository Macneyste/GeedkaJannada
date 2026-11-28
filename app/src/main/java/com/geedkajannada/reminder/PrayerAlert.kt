package com.geedkajannada.reminder

import android.content.Context
import com.geedkajannada.container
import com.geedkajannada.data.NotifyMode
import com.geedkajannada.data.Prayer
import kotlinx.coroutines.flow.first

/** Meesha kaliya ee digniinta salaadda laga bixiyo (alarm-ka iyo badhanka "Tijaabi"). */
object PrayerAlert {
    suspend fun fire(context: Context, prayer: Prayer, earlyMinutes: Int) {
        val store = context.container.settings
        if (!store.notificationsEnabled.first() || !NotificationHelper.hasPermission(context)) return
        val minute = store.prayerEntries.first().firstOrNull { it.prayer == prayer }?.minuteOfDay
        val place = store.prayerCity.first().nameSo
        var mode = store.notifyMode.first()
        // Aadaanka wuxuu dhawaaqaa waqtiga salaadda oo keliya; ogeysiiska hore waa dhawaaq gaaban.
        if (mode == NotifyMode.ADHAN) {
            val ready = earlyMinutes == 0 && AdhanAudio.file(context).exists()
            if (ready && AdhanService.start(context, prayer, minute, place)) return
            mode = NotifyMode.SOUND
        }
        NotificationHelper.showPrayer(context, prayer, earlyMinutes, minute, place, mode)
    }
}
