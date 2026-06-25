package io.github.max_schall.appiary.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.max_schall.appiary.AppiaryApplication
import io.github.max_schall.appiary.MainActivity
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.data.settings.AppPrefs
import java.util.Calendar

/**
 * Daily background job: recompute recommendations, then post a single summary
 * notification of what needs doing — unless disabled or inside quiet hours.
 * Deep-links into the app (Today) on tap.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? AppiaryApplication ?: return Result.success()
        val container = app.container
        val prefs = container.settingsRepository.getAppPrefs()
        if (!prefs.dailySummaryEnabled) return Result.success()

        // Keep the data current even when the app hasn't been opened.
        container.refreshRecommendations()
        // Refresh the home-screen widget with the recomputed state.
        runCatching { io.github.max_schall.appiary.ui.widget.TodayWidget.refresh(applicationContext) }

        if (inQuietHours(prefs)) return Result.success()

        val doNow = container.database.recommendationDao().countActiveInBucket("DO_NOW")
        val dueSoon = container.database.recommendationDao().countActiveInBucket("DUE_SOON")
        if (doNow == 0 && dueSoon == 0) return Result.success()

        postSummary(doNow, dueSoon)
        return Result.success()
    }

    private fun inQuietHours(prefs: AppPrefs): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val start = prefs.quietHoursStart
        val end = prefs.quietHoursEnd
        return if (start <= end) hour in start until end else hour >= start || hour < end
    }

    private fun postSummary(doNow: Int, dueSoon: Int) {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED &&
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
        ) return

        val text = buildString {
            if (doNow > 0) append("$doNow to do now")
            if (doNow > 0 && dueSoon > 0) append(" · ")
            if (dueSoon > 0) append("$dueSoon due soon")
        }
        val tapIntent = Intent(applicationContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(
            applicationContext, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Appiary — today's hive tasks")
            .setContentText(text)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        runCatching { NotificationManagerCompat.from(applicationContext).notify(1001, notification) }
    }
}
