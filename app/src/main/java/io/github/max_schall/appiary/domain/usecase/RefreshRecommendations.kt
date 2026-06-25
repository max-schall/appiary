package io.github.max_schall.appiary.domain.usecase

import io.github.max_schall.appiary.data.climate.ClimateRepository
import io.github.max_schall.appiary.data.climate.WeatherRepository
import io.github.max_schall.appiary.data.dao.ApiaryDao
import io.github.max_schall.appiary.data.dao.HiveDao
import io.github.max_schall.appiary.data.dao.InspectionDao
import io.github.max_schall.appiary.data.dao.ManualTaskDao
import io.github.max_schall.appiary.data.dao.MiteCheckDao
import io.github.max_schall.appiary.data.dao.RecommendationDao
import io.github.max_schall.appiary.data.dao.SeasonalProfileDao
import io.github.max_schall.appiary.data.dao.TreatmentDao
import io.github.max_schall.appiary.data.mapper.RecommendationReconciler
import io.github.max_schall.appiary.domain.rules.ApiaryContext
import io.github.max_schall.appiary.domain.rules.DefaultRuleStrings
import io.github.max_schall.appiary.domain.rules.EvaluationInput
import io.github.max_schall.appiary.domain.rules.HiveContext
import io.github.max_schall.appiary.domain.rules.RuleConfig
import io.github.max_schall.appiary.domain.rules.RuleEngine
import io.github.max_schall.appiary.domain.rules.RuleStrings
import io.github.max_schall.appiary.domain.season.PhenologyEngine
import io.github.max_schall.appiary.domain.season.RegionResolver
import io.github.max_schall.appiary.util.CalendarUtil

/**
 * Orchestrates one engine run: gather live data → build contexts → evaluate →
 * reconcile against persisted recommendations (preserving snooze/dismiss) →
 * write. Call this after any data change that could affect recommendations
 * (inspection saved, treatment logged, task completed, …) and on app start.
 */
class RefreshRecommendations(
    private val hiveDao: HiveDao,
    private val inspectionDao: InspectionDao,
    private val miteCheckDao: MiteCheckDao,
    private val treatmentDao: TreatmentDao,
    private val manualTaskDao: ManualTaskDao,
    private val seasonalProfileDao: SeasonalProfileDao,
    private val recommendationDao: RecommendationDao,
    private val apiaryDao: ApiaryDao,
    private val climateRepository: ClimateRepository,
    private val weatherRepository: WeatherRepository,
    /** Supplies the (user-configurable) thresholds for each run. */
    private val configProvider: suspend () -> RuleConfig = { RuleConfig.DEFAULT },
    /** Supplies the localized text producer for the active language. */
    private val ruleStringsProvider: () -> RuleStrings = { DefaultRuleStrings },
    private val clock: () -> Long = System::currentTimeMillis,
    private val monthOf: (Long) -> Int = CalendarUtil::monthOf,
) {
    /** Number of recent inspections fed to each hive context. */
    private val historyDepth = 6

    suspend operator fun invoke() {
        val now = clock()
        val month = monthOf(now)
        val profile = seasonalProfileDao.getSelected()
        val config = configProvider().copy(strings = ruleStringsProvider())
        val engine = RuleEngine(config)

        val contexts = hiveDao.getActiveHives().map { hive ->
            HiveContext(
                hive = hive,
                recentInspections = inspectionDao.recentForHive(hive.id, historyDepth),
                latestMiteCheck = miteCheckDao.latestForHive(hive.id),
                latestTreatment = treatmentDao.latestForHive(hive.id),
                now = now,
                currentMonth = month,
                seasonal = profile,
            )
        }
        val openTasks = manualTaskDao.getOpenTasks()

        // Apiary-level (seasonal) contexts: phase from the selected profile, climate
        // per-apiary from its site coordinates (cached; null when unavailable/offline).
        val sites = apiaryDao.getAllSites().associateBy { it.id }
        val apiaryContexts = apiaryDao.getAllApiaries().map { apiary ->
            val site = apiary.siteId?.let { sites[it] }
            val climate = if (site?.latitude != null && site.longitude != null) {
                climateRepository.climateProfile(site.latitude, site.longitude)
            } else null
            val calendar = RegionResolver.resolve(site?.latitude, site?.longitude, climate?.group)
            val forecast = if (site?.latitude != null && site.longitude != null) {
                weatherRepository.forecast(site.latitude, site.longitude)
            } else emptyList()
            ApiaryContext(
                apiaryId = apiary.id,
                season = PhenologyEngine.model(month, profile, climate, calendar),
                now = now,
                forecast = forecast,
                countryCode = site?.countryCode,
                treatmentsMissingReceipt = treatmentDao.countMissingReceipt(apiary.id),
            )
        }

        val fresh = engine.evaluate(
            EvaluationInput(
                hiveContexts = contexts,
                apiaryContexts = apiaryContexts,
                openTasks = openTasks,
                now = now,
            ),
        )
        val existing = recommendationDao.getAll()
        val recon = RecommendationReconciler.reconcile(existing, fresh, now)

        if (recon.toUpsert.isNotEmpty()) recommendationDao.upsertAll(recon.toUpsert)
        if (recon.toDeleteIds.isNotEmpty()) recommendationDao.deleteByIds(recon.toDeleteIds)
    }
}
