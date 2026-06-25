package io.github.max_schall.appiary.ui.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.data.backup.BackupManager
import io.github.max_schall.appiary.data.dao.SeasonalProfileDao
import io.github.max_schall.appiary.data.seed.DatabaseSeeder
import io.github.max_schall.appiary.data.settings.AppPrefs
import io.github.max_schall.appiary.data.settings.SettingsRepository
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.usecase.RefreshRecommendations
import io.github.max_schall.appiary.worker.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val settingsRepo: SettingsRepository,
    private val backupManager: BackupManager,
    private val refreshRecommendations: RefreshRecommendations,
    private val seeder: DatabaseSeeder,
    seasonalProfileDao: SeasonalProfileDao,
) : AndroidViewModel(application) {

    val ruleConfig = settingsRepo.ruleConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RuleConfig.DEFAULT)
    val appPrefs = settingsRepo.appPrefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPrefs())
    val seasonalProfileName = seasonalProfileDao.observeSelected().map { it?.name ?: "None" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "None")

    fun updateRule(transform: (RuleConfig) -> RuleConfig) = viewModelScope.launch {
        settingsRepo.updateRuleConfig(transform)
        refreshRecommendations() // thresholds changed → re-evaluate
    }

    fun resetRules() = viewModelScope.launch {
        settingsRepo.resetRuleConfig(); refreshRecommendations()
    }

    fun updatePrefs(transform: (AppPrefs) -> AppPrefs) = viewModelScope.launch {
        settingsRepo.updateAppPrefs(transform)
        val prefs = settingsRepo.getAppPrefs()
        val ctx = getApplication<Application>()
        if (prefs.dailySummaryEnabled) ReminderScheduler.scheduleDaily(ctx, prefs.summaryHour)
        else ReminderScheduler.cancel(ctx)
    }

    fun recomputeNow() = viewModelScope.launch { refreshRecommendations() }

    suspend fun exportJson(): String = backupManager.exportJson()
    suspend fun exportCsv(): String = backupManager.exportInspectionsCsv()

    fun restore(jsonText: String) = viewModelScope.launch {
        backupManager.importJson(jsonText)
        refreshRecommendations()
    }

    fun clearAllData() = viewModelScope.launch {
        seeder.clearAllData()
        refreshRecommendations()
    }
}
