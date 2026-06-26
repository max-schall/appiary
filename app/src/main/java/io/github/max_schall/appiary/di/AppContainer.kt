package io.github.max_schall.appiary.di

import android.content.Context
import io.github.max_schall.appiary.data.backup.BackupManager
import io.github.max_schall.appiary.data.climate.ClimateRepository
import io.github.max_schall.appiary.data.db.AppiaryDatabase
import io.github.max_schall.appiary.data.repository.PhotoRepository
import io.github.max_schall.appiary.data.settings.SettingsRepository
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.FeedingRepository
import io.github.max_schall.appiary.data.repository.HarvestRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.data.repository.InspectionRepository
import io.github.max_schall.appiary.data.repository.MiteCheckRepository
import io.github.max_schall.appiary.data.repository.QueenRecordRepository
import io.github.max_schall.appiary.data.repository.RecommendationRepository
import io.github.max_schall.appiary.data.repository.TaskRepository
import io.github.max_schall.appiary.data.repository.TreatmentRepository
import io.github.max_schall.appiary.data.seed.DatabaseSeeder
import io.github.max_schall.appiary.domain.rules.DefaultRuleStrings
import io.github.max_schall.appiary.domain.rules.GermanRuleStrings
import io.github.max_schall.appiary.domain.usecase.RefreshRecommendations
import io.github.max_schall.appiary.util.AppLocale

/**
 * Tiny manual DI container — no Hilt, intentionally. Holds the database and the
 * repositories as lazy singletons; constructed once in [io.github.max_schall.appiary.AppiaryApplication]
 * and reached from ViewModels via the application context. Keeps wiring explicit
 * and the dependency graph easy to follow.
 */
class AppContainer(private val context: Context) {

    val database: AppiaryDatabase by lazy { AppiaryDatabase.build(context) }

    val settingsRepository by lazy { SettingsRepository(context) }
    val photoRepository by lazy { PhotoRepository(database.photoDao(), context) }
    val backupManager by lazy { BackupManager(database) }
    val climateRepository by lazy { ClimateRepository(cacheDao = database.climateCacheDao()) }
    val weatherRepository by lazy { io.github.max_schall.appiary.data.climate.WeatherRepository() }

    val apiaryRepository by lazy { ApiaryRepository(database.apiaryDao()) }
    val hiveRepository by lazy { HiveRepository(database.hiveDao()) }
    val inspectionRepository by lazy {
        InspectionRepository(database.inspectionDao(), database.hiveDao())
    }
    val miteCheckRepository by lazy {
        MiteCheckRepository(database.miteCheckDao(), database.hiveDao())
    }
    val treatmentRepository by lazy {
        TreatmentRepository(database.treatmentDao(), database.hiveDao())
    }
    val feedingRepository by lazy { FeedingRepository(database.feedingDao()) }
    val harvestRepository by lazy { HarvestRepository(database.harvestDao()) }
    val weightRepository by lazy {
        io.github.max_schall.appiary.data.repository.WeightRepository(database.weightDao())
    }
    val queenRecordRepository by lazy {
        QueenRecordRepository(database.queenRecordDao(), database.hiveDao())
    }
    val inventoryRepository by lazy {
        io.github.max_schall.appiary.data.repository.InventoryRepository(database.inventoryDao())
    }
    val searchRepository by lazy {
        io.github.max_schall.appiary.data.repository.SearchRepository(
            hiveDao = database.hiveDao(),
            inspectionDao = database.inspectionDao(),
            miteCheckDao = database.miteCheckDao(),
            treatmentDao = database.treatmentDao(),
            feedingDao = database.feedingDao(),
            harvestDao = database.harvestDao(),
            taskDao = database.manualTaskDao(),
            inventoryDao = database.inventoryDao(),
        )
    }
    val colonyRepository by lazy {
        io.github.max_schall.appiary.data.repository.ColonyRepository(
            database.colonyEventDao(), database.hiveDao(),
        )
    }
    val analyticsRepository by lazy {
        io.github.max_schall.appiary.data.repository.AnalyticsRepository(
            harvestDao = database.harvestDao(),
            miteCheckDao = database.miteCheckDao(),
            inspectionDao = database.inspectionDao(),
            hiveDao = database.hiveDao(),
        )
    }
    val taskRepository by lazy { TaskRepository(database.manualTaskDao()) }
    val recommendationRepository by lazy { RecommendationRepository(database.recommendationDao()) }
    val receiptRepository by lazy {
        io.github.max_schall.appiary.data.repository.ReceiptRepository(database.medicineReceiptDao(), context)
    }

    val seeder by lazy { DatabaseSeeder(database) }

    val refreshRecommendations by lazy {
        RefreshRecommendations(
            hiveDao = database.hiveDao(),
            inspectionDao = database.inspectionDao(),
            miteCheckDao = database.miteCheckDao(),
            treatmentDao = database.treatmentDao(),
            manualTaskDao = database.manualTaskDao(),
            seasonalProfileDao = database.seasonalProfileDao(),
            recommendationDao = database.recommendationDao(),
            apiaryDao = database.apiaryDao(),
            climateRepository = climateRepository,
            weatherRepository = weatherRepository,
            configProvider = { settingsRepository.getRuleConfig() },
            ruleStringsProvider = {
                if (AppLocale.effectiveLanguageCode() == "de") GermanRuleStrings else DefaultRuleStrings
            },
        )
    }
}
