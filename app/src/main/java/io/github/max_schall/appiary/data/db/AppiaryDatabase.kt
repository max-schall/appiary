package io.github.max_schall.appiary.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.max_schall.appiary.data.dao.ApiaryDao
import io.github.max_schall.appiary.data.dao.FeedingDao
import io.github.max_schall.appiary.data.dao.HarvestDao
import io.github.max_schall.appiary.data.dao.HiveDao
import io.github.max_schall.appiary.data.dao.InspectionDao
import io.github.max_schall.appiary.data.dao.ManualTaskDao
import io.github.max_schall.appiary.data.dao.MiteCheckDao
import io.github.max_schall.appiary.data.dao.PhotoDao
import io.github.max_schall.appiary.data.dao.QueenRecordDao
import io.github.max_schall.appiary.data.dao.RecommendationDao
import io.github.max_schall.appiary.data.dao.ReminderDao
import io.github.max_schall.appiary.data.dao.SeasonalProfileDao
import io.github.max_schall.appiary.data.dao.TreatmentDao
import io.github.max_schall.appiary.data.entity.ApiaryEntity
import io.github.max_schall.appiary.data.entity.ApiarySiteEntity
import io.github.max_schall.appiary.data.entity.FeedingEventEntity
import io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity
import io.github.max_schall.appiary.data.entity.HarvestEventEntity
import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.data.entity.HiveStatusSnapshotEntity
import io.github.max_schall.appiary.data.entity.InspectionEntity
import io.github.max_schall.appiary.data.entity.ManualTaskEntity
import io.github.max_schall.appiary.data.entity.MiteCheckEntity
import io.github.max_schall.appiary.data.entity.PhotoAttachmentEntity
import io.github.max_schall.appiary.data.entity.QueenRecordEntity
import io.github.max_schall.appiary.data.entity.ReminderSettingEntity
import io.github.max_schall.appiary.data.entity.SeasonalProfileEntity
import io.github.max_schall.appiary.data.entity.TreatmentEventEntity

@Database(
    entities = [
        ApiarySiteEntity::class,
        ApiaryEntity::class,
        HiveEntity::class,
        HiveStatusSnapshotEntity::class,
        InspectionEntity::class,
        QueenRecordEntity::class,
        MiteCheckEntity::class,
        TreatmentEventEntity::class,
        FeedingEventEntity::class,
        HarvestEventEntity::class,
        ManualTaskEntity::class,
        GeneratedRecommendationEntity::class,
        PhotoAttachmentEntity::class,
        ReminderSettingEntity::class,
        SeasonalProfileEntity::class,
        io.github.max_schall.appiary.data.entity.ClimateCacheEntity::class,
        io.github.max_schall.appiary.data.entity.MedicineReceiptEntity::class,
        io.github.max_schall.appiary.data.entity.ColonyEventEntity::class,
        io.github.max_schall.appiary.data.entity.WeightEntryEntity::class,
        io.github.max_schall.appiary.data.entity.InventoryItemEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppiaryDatabase : RoomDatabase() {
    abstract fun apiaryDao(): ApiaryDao
    abstract fun hiveDao(): HiveDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun queenRecordDao(): QueenRecordDao
    abstract fun miteCheckDao(): MiteCheckDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun feedingDao(): FeedingDao
    abstract fun harvestDao(): HarvestDao
    abstract fun manualTaskDao(): ManualTaskDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun photoDao(): PhotoDao
    abstract fun reminderDao(): ReminderDao
    abstract fun seasonalProfileDao(): SeasonalProfileDao
    abstract fun climateCacheDao(): io.github.max_schall.appiary.data.dao.ClimateCacheDao
    abstract fun medicineReceiptDao(): io.github.max_schall.appiary.data.dao.MedicineReceiptDao
    abstract fun colonyEventDao(): io.github.max_schall.appiary.data.dao.ColonyEventDao
    abstract fun weightDao(): io.github.max_schall.appiary.data.dao.WeightDao
    abstract fun inventoryDao(): io.github.max_schall.appiary.data.dao.InventoryDao

    companion object {
        /** v1 → v2: location columns on seasonal_profiles (for location-derived seasons). */
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE seasonal_profiles ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE seasonal_profiles ADD COLUMN longitude REAL")
            }
        }

        /** v2 → v3: cache of derived climate classification per location. */
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `climate_cache` (" +
                        "`locationKey` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, " +
                        "`koppen` TEXT NOT NULL, `koppenGroup` TEXT NOT NULL, `hardinessZone` INTEGER NOT NULL, " +
                        "`annualMinTempC` REAL NOT NULL, `monthlyTempsCsv` TEXT NOT NULL, " +
                        "`monthlyPrecipCsv` TEXT NOT NULL, `fetchedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`locationKey`))",
                )
            }
        }

        /** v3 → v4: German Bestandsbuch — receipts, treatment proof fields, site country. */
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE apiary_sites ADD COLUMN countryCode TEXT")
                db.execSQL("ALTER TABLE treatment_events ADD COLUMN receiptId TEXT")
                db.execSQL("ALTER TABLE treatment_events ADD COLUMN productName TEXT")
                db.execSQL("ALTER TABLE treatment_events ADD COLUMN withdrawalDays INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `medicine_receipts` (" +
                        "`id` TEXT NOT NULL, `photoUri` TEXT, `supplierName` TEXT, `supplierAddress` TEXT, " +
                        "`purchaseDate` INTEGER, `label` TEXT, `vetName` TEXT, `vetContact` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
                )
            }
        }

        /** v4 → v5: colony lifecycle — hive lineage columns + colony_events log. */
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE hives ADD COLUMN originType TEXT NOT NULL DEFAULT 'UNKNOWN'")
                db.execSQL("ALTER TABLE hives ADD COLUMN parentHiveId TEXT")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `colony_events` (" +
                        "`id` TEXT NOT NULL, `hiveId` TEXT NOT NULL, `apiaryId` TEXT NOT NULL, " +
                        "`type` TEXT NOT NULL, `relatedHiveId` TEXT, `relatedHiveName` TEXT, " +
                        "`occurredAt` INTEGER NOT NULL, `notes` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`hiveId`) REFERENCES `hives`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_colony_events_hiveId` ON `colony_events` (`hiveId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_colony_events_relatedHiveId` ON `colony_events` (`relatedHiveId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_colony_events_occurredAt` ON `colony_events` (`occurredAt`)")
            }
        }

        /** v5 → v6: hive-weight readings (scale series). */
        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `weight_entries` (" +
                        "`id` TEXT NOT NULL, `hiveId` TEXT NOT NULL, `apiaryId` TEXT NOT NULL, " +
                        "`recordedAt` INTEGER NOT NULL, `weightKg` REAL NOT NULL, `notes` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`hiveId`) REFERENCES `hives`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_entries_hiveId` ON `weight_entries` (`hiveId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_entries_apiaryId` ON `weight_entries` (`apiaryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_entries_recordedAt` ON `weight_entries` (`recordedAt`)")
            }
        }

        /** v6 → v7: equipment/supply inventory. */
        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `inventory_items` (" +
                        "`id` TEXT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, " +
                        "`quantity` REAL NOT NULL, `unit` TEXT, `lowStockThreshold` REAL, `notes` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_items_category` ON `inventory_items` (`category`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_items_name` ON `inventory_items` (`name`)")
            }
        }

        fun build(context: Context): AppiaryDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppiaryDatabase::class.java,
                "appiary.db",
            )
                .addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                    MIGRATION_6_7,
                )
                .build()
    }
}
