package io.github.max_schall.appiary.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.max_schall.appiary.AppiaryApplication
import io.github.max_schall.appiary.di.AppContainer
import io.github.max_schall.appiary.ui.screen.apiaries.ApiariesViewModel
import io.github.max_schall.appiary.ui.screen.apiaries.ApiaryDetailViewModel
import io.github.max_schall.appiary.ui.screen.hives.HiveDetailViewModel
import io.github.max_schall.appiary.ui.screen.hives.HivesViewModel
import io.github.max_schall.appiary.ui.screen.log.FeedingViewModel
import io.github.max_schall.appiary.ui.screen.log.HarvestViewModel
import io.github.max_schall.appiary.ui.screen.log.InspectionViewModel
import io.github.max_schall.appiary.ui.screen.log.MiteCheckViewModel
import io.github.max_schall.appiary.ui.screen.log.NoteViewModel
import io.github.max_schall.appiary.ui.screen.log.TreatmentViewModel
import io.github.max_schall.appiary.ui.screen.photo.PhotoCaptureViewModel
import io.github.max_schall.appiary.ui.screen.settings.SeasonalProfileViewModel
import io.github.max_schall.appiary.ui.screen.settings.SettingsViewModel
import io.github.max_schall.appiary.ui.screen.tasks.TasksViewModel
import io.github.max_schall.appiary.ui.screen.today.TodayViewModel

/** Reaches the manual DI container from a ViewModel's creation extras. */
fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as AppiaryApplication).container

/**
 * Central ViewModel factory — wires repositories from [AppContainer] into each
 * ViewModel. Detail ViewModels read their id from the navigation arg via
 * [SavedStateHandle]. No Hilt; the graph stays explicit.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val c = container()
            TodayViewModel(c.recommendationRepository, c.apiaryRepository, c.hiveRepository)
        }
        initializer {
            ApiariesViewModel(container().apiaryRepository)
        }
        initializer {
            io.github.max_schall.appiary.ui.screen.analytics.AnalyticsViewModel(container().analyticsRepository)
        }
        initializer {
            io.github.max_schall.appiary.ui.screen.map.MapViewModel(container().apiaryRepository)
        }
        initializer {
            val c = container()
            HivesViewModel(c.hiveRepository, c.apiaryRepository, c.recommendationRepository)
        }
        initializer {
            val c = container()
            ApiaryDetailViewModel(
                this[APPLICATION_KEY] as AppiaryApplication,
                createSavedStateHandle(),
                c.apiaryRepository, c.hiveRepository, c.recommendationRepository, c.colonyRepository,
            )
        }
        initializer {
            val c = container()
            HiveDetailViewModel(
                this[APPLICATION_KEY] as AppiaryApplication,
                createSavedStateHandle(),
                c.hiveRepository, c.recommendationRepository, c.colonyRepository, c.weightRepository,
                c.inspectionRepository, c.miteCheckRepository, c.treatmentRepository,
                c.feedingRepository, c.harvestRepository, c.queenRecordRepository,
                c.photoRepository,
            )
        }
        initializer {
            val c = container()
            TasksViewModel(c.taskRepository, c.recommendationRepository, c.hiveRepository, c.apiaryRepository)
        }

        // --- Logging flows ---
        initializer {
            val c = container()
            InspectionViewModel(
                createSavedStateHandle(), c.hiveRepository, c.apiaryRepository,
                c.inspectionRepository, c.taskRepository, c.refreshRecommendations,
            )
        }
        initializer {
            val c = container()
            MiteCheckViewModel(
                createSavedStateHandle(), c.hiveRepository, c.apiaryRepository,
                c.miteCheckRepository, c.refreshRecommendations,
            )
        }
        initializer {
            val c = container()
            TreatmentViewModel(
                createSavedStateHandle(), c.hiveRepository, c.apiaryRepository,
                c.treatmentRepository, c.refreshRecommendations,
            )
        }
        initializer {
            val c = container()
            FeedingViewModel(
                createSavedStateHandle(), c.hiveRepository, c.apiaryRepository,
                c.feedingRepository, c.refreshRecommendations,
            )
        }
        initializer {
            val c = container()
            HarvestViewModel(
                createSavedStateHandle(), c.hiveRepository, c.apiaryRepository,
                c.harvestRepository, c.refreshRecommendations,
            )
        }
        initializer {
            val c = container()
            NoteViewModel(
                this[APPLICATION_KEY] as AppiaryApplication,
                createSavedStateHandle(), c.hiveRepository, c.apiaryRepository,
                c.taskRepository, c.refreshRecommendations,
            )
        }
        initializer { PhotoCaptureViewModel(createSavedStateHandle(), container().photoRepository) }
        initializer {
            val c = container()
            io.github.max_schall.appiary.ui.screen.recordbook.RecordBookViewModel(
                this[APPLICATION_KEY] as AppiaryApplication,
                createSavedStateHandle(),
                c.apiaryRepository, c.hiveRepository, c.treatmentRepository,
                c.receiptRepository, c.refreshRecommendations,
            )
        }
        initializer {
            val c = container()
            SettingsViewModel(
                this[APPLICATION_KEY] as AppiaryApplication,
                c.settingsRepository, c.backupManager, c.refreshRecommendations,
                c.seeder, c.database.seasonalProfileDao(),
            )
        }
        initializer {
            val c = container()
            SeasonalProfileViewModel(
                this[APPLICATION_KEY] as AppiaryApplication,
                c.database.seasonalProfileDao(), c.climateRepository, c.refreshRecommendations,
            )
        }
    }
}
