package io.github.max_schall.appiary.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.max_schall.appiary.domain.rules.RuleConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("appiary_settings")

/** App theme selection. Defaults to following the system light/dark setting. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Notification + display preferences not owned by the rules engine. */
data class AppPrefs(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val nfcEnabled: Boolean = false,
    val dailySummaryEnabled: Boolean = true,
    val summaryHour: Int = 8,
    val quietHoursStart: Int = 22,
    val quietHoursEnd: Int = 7,
)

/**
 * Single source of truth for tunable settings, persisted in Preferences
 * DataStore. Exposes the rules-engine [RuleConfig] and app [AppPrefs] as flows
 * plus suspend getters/updaters. Missing keys fall back to code defaults.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val INSPECT_ACTIVE = intPreferencesKey("rule_inspect_active")
        val INSPECT_OFF = intPreferencesKey("rule_inspect_off")
        val DUE_SOON = intPreferencesKey("rule_due_soon_window")
        val INSPECT_DONOW = intPreferencesKey("rule_inspect_donow")
        val QUEEN_FOLLOWUP = intPreferencesKey("rule_queen_followup")
        val QUEEN_REPEAT = intPreferencesKey("rule_queen_repeat")
        val SWARM_FOLLOWUP = intPreferencesKey("rule_swarm_followup")
        val MITE_INTERVAL = intPreferencesKey("rule_mite_interval")
        val WATCH_WEAK = booleanPreferencesKey("rule_watch_weak")
        val INSPECT_MIN_TEMP = doublePreferencesKey("rule_inspect_min_temp")
        val TREAT_HEAT_MAX = doublePreferencesKey("rule_treat_heat_max")
        val COLD_SNAP_MIN = doublePreferencesKey("rule_cold_snap_min")

        val THEME_MODE = stringPreferencesKey("app_theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("app_dynamic_color")
        val NFC_ENABLED = booleanPreferencesKey("app_nfc_enabled")
        val DAILY_SUMMARY = booleanPreferencesKey("app_daily_summary")
        val SUMMARY_HOUR = intPreferencesKey("app_summary_hour")
        val QUIET_START = intPreferencesKey("app_quiet_start")
        val QUIET_END = intPreferencesKey("app_quiet_end")
    }

    val ruleConfig: Flow<RuleConfig> = context.dataStore.data.map { it.toRuleConfig() }
    val appPrefs: Flow<AppPrefs> = context.dataStore.data.map { it.toAppPrefs() }

    suspend fun getRuleConfig(): RuleConfig = ruleConfig.first()
    suspend fun getAppPrefs(): AppPrefs = appPrefs.first()

    suspend fun updateRuleConfig(transform: (RuleConfig) -> RuleConfig) {
        context.dataStore.edit { prefs ->
            val c = transform(prefs.toRuleConfig())
            prefs[Keys.INSPECT_ACTIVE] = c.activeSeasonInspectionIntervalDays
            prefs[Keys.INSPECT_OFF] = c.offSeasonInspectionIntervalDays
            prefs[Keys.DUE_SOON] = c.dueSoonWindowDays
            prefs[Keys.INSPECT_DONOW] = c.inspectionDoNowOverdueDays
            prefs[Keys.QUEEN_FOLLOWUP] = c.queenFollowUpDays
            prefs[Keys.QUEEN_REPEAT] = c.repeatedQueenUncertaintyCount
            prefs[Keys.SWARM_FOLLOWUP] = c.swarmFollowUpDays
            prefs[Keys.MITE_INTERVAL] = c.miteCheckIntervalDays
            prefs[Keys.WATCH_WEAK] = c.watchWeakColonies
            prefs[Keys.INSPECT_MIN_TEMP] = c.inspectionMinTempC
            prefs[Keys.TREAT_HEAT_MAX] = c.treatmentHeatMaxTempC
            prefs[Keys.COLD_SNAP_MIN] = c.coldSnapMinTempC
        }
    }

    suspend fun updateAppPrefs(transform: (AppPrefs) -> AppPrefs) {
        context.dataStore.edit { prefs ->
            val p = transform(prefs.toAppPrefs())
            prefs[Keys.THEME_MODE] = p.themeMode.name
            prefs[Keys.DYNAMIC_COLOR] = p.dynamicColor
            prefs[Keys.NFC_ENABLED] = p.nfcEnabled
            prefs[Keys.DAILY_SUMMARY] = p.dailySummaryEnabled
            prefs[Keys.SUMMARY_HOUR] = p.summaryHour
            prefs[Keys.QUIET_START] = p.quietHoursStart
            prefs[Keys.QUIET_END] = p.quietHoursEnd
        }
    }

    suspend fun resetRuleConfig() = updateRuleConfig { RuleConfig.DEFAULT }

    private fun Preferences.toRuleConfig(): RuleConfig {
        val d = RuleConfig.DEFAULT
        return RuleConfig(
            activeSeasonInspectionIntervalDays = this[Keys.INSPECT_ACTIVE] ?: d.activeSeasonInspectionIntervalDays,
            offSeasonInspectionIntervalDays = this[Keys.INSPECT_OFF] ?: d.offSeasonInspectionIntervalDays,
            dueSoonWindowDays = this[Keys.DUE_SOON] ?: d.dueSoonWindowDays,
            inspectionDoNowOverdueDays = this[Keys.INSPECT_DONOW] ?: d.inspectionDoNowOverdueDays,
            queenFollowUpDays = this[Keys.QUEEN_FOLLOWUP] ?: d.queenFollowUpDays,
            repeatedQueenUncertaintyCount = this[Keys.QUEEN_REPEAT] ?: d.repeatedQueenUncertaintyCount,
            swarmFollowUpDays = this[Keys.SWARM_FOLLOWUP] ?: d.swarmFollowUpDays,
            miteCheckIntervalDays = this[Keys.MITE_INTERVAL] ?: d.miteCheckIntervalDays,
            watchWeakColonies = this[Keys.WATCH_WEAK] ?: d.watchWeakColonies,
            inspectionMinTempC = this[Keys.INSPECT_MIN_TEMP] ?: d.inspectionMinTempC,
            treatmentHeatMaxTempC = this[Keys.TREAT_HEAT_MAX] ?: d.treatmentHeatMaxTempC,
            coldSnapMinTempC = this[Keys.COLD_SNAP_MIN] ?: d.coldSnapMinTempC,
        )
    }

    private fun Preferences.toAppPrefs(): AppPrefs {
        val d = AppPrefs()
        return AppPrefs(
            themeMode = this[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: d.themeMode,
            dynamicColor = this[Keys.DYNAMIC_COLOR] ?: d.dynamicColor,
            nfcEnabled = this[Keys.NFC_ENABLED] ?: d.nfcEnabled,
            dailySummaryEnabled = this[Keys.DAILY_SUMMARY] ?: d.dailySummaryEnabled,
            summaryHour = this[Keys.SUMMARY_HOUR] ?: d.summaryHour,
            quietHoursStart = this[Keys.QUIET_START] ?: d.quietHoursStart,
            quietHoursEnd = this[Keys.QUIET_END] ?: d.quietHoursEnd,
        )
    }
}
