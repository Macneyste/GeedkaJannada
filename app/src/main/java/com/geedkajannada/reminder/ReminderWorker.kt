package com.geedkajannada.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.geedkajannada.container
import com.geedkajannada.data.Prayer

/** Habka inexact (exact alarm lama oggola): WorkManager. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val prayer = Prayer.entries.firstOrNull { it.key == inputData.getString(ReminderScheduler.EXTRA_PRAYER) }
            ?: return Result.failure()
        val offset = inputData.getInt(ReminderScheduler.EXTRA_OFFSET, 0)
        showFromStore(applicationContext, prayer, offset)
        ReminderScheduler.rescheduleAll(applicationContext, applicationContext.container.settings)
        return Result.success()
    }
}
