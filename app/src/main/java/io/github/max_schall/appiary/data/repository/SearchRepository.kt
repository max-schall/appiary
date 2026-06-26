package io.github.max_schall.appiary.data.repository

import io.github.max_schall.appiary.data.dao.FeedingDao
import io.github.max_schall.appiary.data.dao.HarvestDao
import io.github.max_schall.appiary.data.dao.HiveDao
import io.github.max_schall.appiary.data.dao.InspectionDao
import io.github.max_schall.appiary.data.dao.InventoryDao
import io.github.max_schall.appiary.data.dao.ManualTaskDao
import io.github.max_schall.appiary.data.dao.MiteCheckDao
import io.github.max_schall.appiary.data.dao.TreatmentDao

/** What a [SearchResult] points at — drives its icon and how a tap navigates. */
enum class SearchResultKind { HIVE, INSPECTION, MITE_CHECK, TREATMENT, FEEDING, HARVEST, TASK, INVENTORY }

/**
 * One global-search hit. [primaryText] is the matched text (or item name),
 * [hiveName] gives context, and [hiveId] (when present) lets a tap open the hive.
 */
data class SearchResult(
    val kind: SearchResultKind,
    val primaryText: String,
    val hiveId: String?,
    val hiveName: String?,
    val timestamp: Long,
)

/**
 * Free-text search across every record type. The local database is small enough
 * that loading each table once and filtering in memory is both simple and fast;
 * it also keeps this off any single DAO. Results are newest-first and capped.
 */
class SearchRepository(
    private val hiveDao: HiveDao,
    private val inspectionDao: InspectionDao,
    private val miteCheckDao: MiteCheckDao,
    private val treatmentDao: TreatmentDao,
    private val feedingDao: FeedingDao,
    private val harvestDao: HarvestDao,
    private val taskDao: ManualTaskDao,
    private val inventoryDao: InventoryDao,
) {
    suspend fun search(rawQuery: String, limit: Int = 60): List<SearchResult> {
        val q = rawQuery.trim()
        if (q.length < 2) return emptyList()
        val needle = q.lowercase()
        fun String?.hit() = this?.lowercase()?.contains(needle) == true

        val hives = hiveDao.getAllHives()
        val hiveNames = hives.associate { it.id to it.name }
        val results = mutableListOf<SearchResult>()

        hives.filter { it.name.hit() }.forEach {
            results += SearchResult(SearchResultKind.HIVE, it.name, it.id, it.name, it.updatedAt)
        }
        inspectionDao.getAll().filter { it.notes.hit() }.forEach {
            results += SearchResult(
                SearchResultKind.INSPECTION, it.notes.orEmpty(), it.hiveId, hiveNames[it.hiveId], it.performedAt,
            )
        }
        miteCheckDao.getAll().filter { it.notes.hit() }.forEach {
            results += SearchResult(
                SearchResultKind.MITE_CHECK, it.notes.orEmpty(), it.hiveId, hiveNames[it.hiveId], it.checkedAt,
            )
        }
        treatmentDao.getAll().filter { it.notes.hit() || it.productName.hit() }.forEach {
            results += SearchResult(
                SearchResultKind.TREATMENT, it.productName ?: it.notes.orEmpty(),
                it.hiveId, hiveNames[it.hiveId], it.startedAt,
            )
        }
        feedingDao.getAll().filter { it.notes.hit() }.forEach {
            results += SearchResult(
                SearchResultKind.FEEDING, it.notes.orEmpty(), it.hiveId, hiveNames[it.hiveId], it.fedAt,
            )
        }
        harvestDao.getAll().filter { it.notes.hit() || it.batchNote.hit() }.forEach {
            results += SearchResult(
                SearchResultKind.HARVEST, it.batchNote ?: it.notes.orEmpty(),
                it.hiveId, it.hiveId?.let(hiveNames::get), it.harvestedAt,
            )
        }
        taskDao.getAll().filter { it.title.hit() || it.details.hit() }.forEach {
            results += SearchResult(
                SearchResultKind.TASK, it.title, it.hiveId, it.hiveId?.let(hiveNames::get),
                it.dueAt ?: it.createdAt,
            )
        }
        inventoryDao.getAll().filter { it.name.hit() || it.notes.hit() }.forEach {
            results += SearchResult(SearchResultKind.INVENTORY, it.name, null, null, it.updatedAt)
        }

        return results.sortedByDescending { it.timestamp }.take(limit)
    }
}
