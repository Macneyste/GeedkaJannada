package com.geedkajannada.reminder

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.geedkajannada.MainActivity
import com.geedkajannada.R
import com.geedkajannada.data.NotifyMode
import com.geedkajannada.data.Prayer
import com.geedkajannada.data.PrayerMath

object NotificationHelper {
    private val oldChannels = listOf("prayer_reminders", "prayer_reminders_v2")
    private val vibration = longArrayOf(0, 400, 200, 400)

    fun channelId(mode: NotifyMode): String = "salaad_${mode.key}"

    fun ensureChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        oldChannels.forEach(nm::deleteNotificationChannel)
        val sound = Uri.parse("android.resource://${context.packageName}/${R.raw.notify_soft}")
        val audio = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        NotifyMode.entries.forEach { mode ->
            val name = context.getString(
                when (mode) {
                    NotifyMode.SOUND -> R.string.notif_channel_sound
                    NotifyMode.VIBRATE -> R.string.notif_channel_vibrate
                    NotifyMode.SILENT -> R.string.notif_channel_silent
                    NotifyMode.ADHAN -> R.string.notif_channel_adhan
                },
            )
            val channel = NotificationChannel(channelId(mode), name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = context.getString(R.string.notif_channel_desc)
                setSound(if (mode == NotifyMode.SOUND) sound else null, if (mode == NotifyMode.SOUND) audio else null)
                enableVibration(mode != NotifyMode.SILENT)
                if (mode != NotifyMode.SILENT) vibrationPattern = vibration
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun notificationId(prayer: Prayer): Int = prayer.ordinal + 100

    /** earlyMinutes = 0: waqtigii salaadda; > 0: ogeysiis hore. stop != null: badhan "Jooji" (aadaan). */
    fun prayerNotification(
        context: Context,
        prayer: Prayer,
        earlyMinutes: Int,
        minuteOfDay: Int?,
        place: String,
        mode: NotifyMode,
        stop: PendingIntent? = null,
    ): Notification {
        ensureChannels(context)
        val name = context.getString(prayer.nameRes)
        val title = if (earlyMinutes == 0) {
            context.getString(R.string.notif_title_now, name)
        } else {
            context.getString(R.string.notif_title_before, name, earlyMinutes)
        }
        val time = minuteOfDay?.let { PrayerMath.format(it) } ?: ""
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val builder = NotificationCompat.Builder(context, channelId(mode))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.notif_body, time, place))
            .setContentIntent(open)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
        if (stop != null) builder.addAction(0, context.getString(R.string.notif_stop), stop)
        builder.addAction(0, context.getString(R.string.notif_open), open)
        return builder.build()
    }

    fun showPrayer(
        context: Context,
        prayer: Prayer,
        earlyMinutes: Int,
        minuteOfDay: Int?,
        place: String,
        mode: NotifyMode,
    ) {
        if (!hasPermission(context)) return
        val n = prayerNotification(context, prayer, earlyMinutes, minuteOfDay, place, mode)
        NotificationManagerCompat.from(context).notify(notificationId(prayer), n)
    }
}
