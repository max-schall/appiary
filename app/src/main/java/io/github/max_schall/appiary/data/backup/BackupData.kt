package io.github.max_schall.appiary.data.backup

import io.github.max_schall.appiary.data.entity.ApiaryEntity
import io.github.max_schall.appiary.data.entity.ApiarySiteEntity
import io.github.max_schall.appiary.data.entity.FeedingEventEntity
import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity
import io.github.max_schall.appiary.data.entity.HarvestEventEntity
import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.data.entity.HiveStatusSnapshotEntity
import io.github.max_schall.appiary.data.entity.InspectionEntity
import io.github.max_schall.appiary.data.entity.ManualTaskEntity
import io.github.max_schall.appiary.data.entity.MedicineReceiptEntity
import io.github.max_schall.appiary.data.entity.MiteCheckEntity
import io.github.max_schall.appiary.data.entity.PhotoAttachmentEntity
import io.github.max_schall.appiary.data.entity.QueenRecordEntity
import io.github.max_schall.appiary.data.entity.ReminderSettingEntity
import io.github.max_schall.appiary.data.entity.SeasonalProfileEntity
import io.github.max_schall.appiary.data.entity.TreatmentEventEntity
import kotlinx.serialization.Serializable

/** A complete, portable snapshot of the local database for JSON backup. */
@Serializable
data class BackupData(
    val schemaVersion: Int = 1,
    val exportedAt: Long,
    val sites: List<ApiarySiteEntity> = emptyList(),
    val apiaries: List<ApiaryEntity> = emptyList(),
    val hives: List<HiveEntity> = emptyList(),
    val snapshots: List<HiveStatusSnapshotEntity> = emptyList(),
    val inspections: List<InspectionEntity> = emptyList(),
    val queenRecords: List<QueenRecordEntity> = emptyList(),
    val miteChecks: List<MiteCheckEntity> = emptyList(),
    val treatments: List<TreatmentEventEntity> = emptyList(),
    val feedings: List<FeedingEventEntity> = emptyList(),
    val harvests: List<HarvestEventEntity> = emptyList(),
    val tasks: List<ManualTaskEntity> = emptyList(),
    val recommendations: List<GeneratedRecommendationEntity> = emptyList(),
    val photos: List<PhotoAttachmentEntity> = emptyList(),
    val reminders: List<ReminderSettingEntity> = emptyList(),
    val profiles: List<SeasonalProfileEntity> = emptyList(),
    val receipts: List<MedicineReceiptEntity> = emptyList(),
    val colonyEvents: List<io.github.max_schall.appiary.data.entity.ColonyEventEntity> = emptyList(),
    val weightEntries: List<io.github.max_schall.appiary.data.entity.WeightEntryEntity> = emptyList(),
    val inventoryItems: List<io.github.max_schall.appiary.data.entity.InventoryItemEntity> = emptyList(),
)
