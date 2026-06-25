package io.github.max_schall.appiary.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** Sets up the reminders notification channel and the daily-summary work. */
object ReminderScheduler {
    const val CHANNEL_ID = "appiary_reminders"
    private const val DAILY_WORK = "appiary_daily_summary"

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, "Hive reminders", NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "Daily summary of what needs doing in your apiaries" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Schedule (or reschedule) the daily summary for [hourOfDay]. */
    fun scheduleDaily(context: Context, hourOfDay: Int) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis(hourOfDay), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(DAILY_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_WORK)
    }

    /** Millis from now until the next occurrence of [hourOfDay]:00 local time. */
    private fun initialDelayMillis(hourOfDay: Int): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }
}
