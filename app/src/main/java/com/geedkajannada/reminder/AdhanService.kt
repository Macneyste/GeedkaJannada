package com.geedkajannada.reminder

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import com.geedkajannada.data.NotifyMode
import com.geedkajannada.data.Prayer

/** Wuxuu dhawaaqaa faylka aadaanka oo dhan, isagoo leh ogeysiin "Jooji" ah. */
class AdhanService : Service() {
    private var player: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == ACTION_STOP) {
            finish(keepNotification = false)
            return START_NOT_STICKY
        }
        val prayer = Prayer.entries.firstOrNull { it.key == intent.getStringExtra(EXTRA_PRAYER) } ?: Prayer.SUBAX
        val minute = intent.getIntExtra(EXTRA_MINUTE, -1).takeIf { it >= 0 }
        val stop = PendingIntent.getService(
            this, 1, Intent(this, AdhanService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationHelper.prayerNotification(
            this, prayer, 0, minute, intent.getStringExtra(EXTRA_PLACE).orEmpty(), NotifyMode.ADHAN, stop,
        )
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0
        ServiceCompat.startForeground(this, NotificationHelper.notificationId(prayer), notification, type)
        play()
        return START_NOT_STICKY
    }

    private fun play() {
        player?.release()
        player = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setDataSource(AdhanAudio.file(this@AdhanService).absolutePath)
                setOnCompletionListener { finish(keepNotification = true) }
                prepare()
                start()
            }
        }.getOrNull()
        if (player == null) finish(keepNotification = true)
    }

    private fun finish(keepNotification: Boolean) {
        player?.release()
        player = null
        ServiceCompat.stopForeground(
            this,
            if (keepNotification) ServiceCompat.STOP_FOREGROUND_DETACH else ServiceCompat.STOP_FOREGROUND_REMOVE,
        )
        stopSelf()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }

    companion object {
        private const val ACTION_STOP = "com.geedkajannada.STOP_ADHAN"
        private const val EXTRA_PRAYER = "prayer"
        private const val EXTRA_MINUTE = "minute"
        private const val EXTRA_PLACE = "place"

        /** false haddii nidaamku diido bilaabidda (markaas ogeysiin caadi ah ayaa la tusaa). */
        fun start(context: Context, prayer: Prayer, minuteOfDay: Int?, place: String): Boolean = runCatching {
            val intent = Intent(context, AdhanService::class.java)
                .putExtra(EXTRA_PRAYER, prayer.key)
                .putExtra(EXTRA_MINUTE, minuteOfDay ?: -1)
                .putExtra(EXTRA_PLACE, place)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }.isSuccess
    }
}
