package io.github.max_schall.appiary.data.backup

import androidx.room.withTransaction
import io.github.max_schall.appiary.data.db.AppiaryDatabase
import kotlinx.serialization.json.Json

/**
 * Full local backup/restore as JSON, plus CSV export of key record tables.
 * Local-first: no cloud, no account — the caller writes the returned string to
 * a user-chosen file (Storage Access Framework) and reads it back for restore.
 */
class BackupManager(private val db: AppiaryDatabase) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun export(): BackupData = BackupData(
        exportedAt = System.currentTimeMillis(),
        sites = db.apiaryDao().getAllSites(),
        apiaries = db.apiaryDao().getAllApiaries(),
        hives = db.hiveDao().getAllHives(),
        snapshots = db.hiveDao().getAllSnapshots(),
        inspections = db.inspectionDao().getAll(),
        queenRecords = db.queenRecordDao().getAll(),
        miteChecks = db.miteCheckDao().getAll(),
        treatments = db.treatmentDao().getAll(),
        feedings = db.feedingDao().getAll(),
        harvests = db.harvestDao().getAll(),
        tasks = db.manualTaskDao().getAll(),
        recommendations = db.recommendationDao().getAll(),
        photos = db.photoDao().getAll(),
        reminders = db.reminderDao().getAll(),
        profiles = db.seasonalProfileDao().getAll(),
        receipts = db.medicineReceiptDao().getAll(),
        colonyEvents = db.colonyEventDao().getAll(),
        weightEntries = db.weightDao().getAll(),
        inventoryItems = db.inventoryDao().getAll(),
    )

    suspend fun exportJson(): String = json.encodeToString(BackupData.serializer(), export())

    /** Replace all local data with the contents of [jsonText]. */
    suspend fun importJson(jsonText: String) {
        val data = json.decodeFromString(BackupData.serializer(), jsonText)
        db.withTransaction {
            db.clearAllTables()
            // FK-safe insert order: parents before children.
            db.apiaryDao().upsertSites(data.sites)
            db.apiaryDao().upsertAll(data.apiaries)
            db.hiveDao().upsertAll(data.hives)
            db.hiveDao().upsertSnapshots(data.snapshots)
            db.inspectionDao().upsertAll(data.inspections)
            db.queenRecordDao().upsertAll(data.queenRecords)
            db.miteCheckDao().upsertAll(data.miteChecks)
            db.medicineReceiptDao().upsertAll(data.receipts)
            db.treatmentDao().upsertAll(data.treatments)
            db.feedingDao().upsertAll(data.feedings)
            db.harvestDao().upsertAll(data.harvests)
            db.manualTaskDao().upsertAll(data.tasks)
            db.recommendationDao().upsertAll(data.recommendations)
            data.photos.forEach { db.photoDao().upsert(it) }
            db.reminderDao().upsertAll(data.reminders)
            db.seasonalProfileDao().upsertAll(data.profiles)
            db.colonyEventDao().upsertAll(data.colonyEvents)
            db.weightDao().upsertAll(data.weightEntries)
            db.inventoryDao().upsertAll(data.inventoryItems)
        }
    }

    /** CSV of inspections — the highest-value table for spreadsheets. */
    suspend fun exportInspectionsCsv(): String = buildString {
        appendLine("date,hiveId,apiaryId,queenSeen,eggsSeen,brood,strength,temperament,food,swarmSigns,queenCells,disease,pests,notes")
        db.inspectionDao().getAll().sortedByDescending { it.performedAt }.forEach { i ->
            appendLine(
                listOf(
                    i.performedAt.toString(), i.hiveId, i.apiaryId, i.queenSeen, i.eggsSeen,
                    i.broodPattern, i.strength, i.temperament, i.foodStores, i.swarmSigns,
                    i.queenCells, i.diseaseConcern, i.pests, i.notes.orEmpty(),
                ).joinToString(",") { csvCell(it.toString()) },
            )
        }
    }

    /** CSV of harvests — yields per product/batch for the spreadsheet crowd. */
    suspend fun exportHarvestsCsv(): String = buildString {
        appendLine("date,hiveId,apiaryId,product,amountKg,batch,notes")
        db.harvestDao().getAll().sortedByDescending { it.harvestedAt }.forEach { h ->
            appendLine(
                listOf(
                    h.harvestedAt.toString(), h.hiveId.orEmpty(), h.apiaryId, h.product,
                    h.amountKg?.toString().orEmpty(), h.batchNote.orEmpty(), h.notes.orEmpty(),
                ).joinToString(",") { csvCell(it.toString()) },
            )
        }
    }

    /** CSV of the equipment/supply inventory — a stock-take you can print. */
    suspend fun exportInventoryCsv(): String = buildString {
        appendLine("name,category,quantity,unit,lowStockThreshold,notes")
        db.inventoryDao().getAll().sortedWith(compareBy({ it.category }, { it.name })).forEach { i ->
            appendLine(
                listOf(
                    i.name, i.category, i.quantity.toString(), i.unit.orEmpty(),
                    i.lowStockThreshold?.toString().orEmpty(), i.notes.orEmpty(),
                ).joinToString(",") { csvCell(it.toString()) },
            )
        }
    }

    private fun csvCell(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' }) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else value
}
