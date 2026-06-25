package io.github.max_schall.appiary

import android.app.Application
import io.github.max_schall.appiary.di.AppContainer
import io.github.max_schall.appiary.worker.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppiaryApplication : Application() {

    lateinit var container: AppContainer
        private set

    /** App-lifetime scope for startup work (seeding, later: engine refresh). */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderScheduler.ensureChannel(this)
        appScope.launch {
            container.seeder.ensureDefaults()
            // Recompute recommendations on startup so Today is current.
            container.refreshRecommendations()
            // Keep any placed home-screen widget in sync with the fresh state.
            runCatching { io.github.max_schall.appiary.ui.widget.TodayWidget.refresh(this@AppiaryApplication) }
            // Schedule (or cancel) the daily summary per saved preference.
            val prefs = container.settingsRepository.getAppPrefs()
            if (prefs.dailySummaryEnabled) {
                ReminderScheduler.scheduleDaily(this@AppiaryApplication, prefs.summaryHour)
            } else {
                ReminderScheduler.cancel(this@AppiaryApplication)
            }
        }
    }
}
