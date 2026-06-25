package io.github.max_schall.appiary.ui.screen.hives

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.data.entity.PhotoAttachmentEntity
import io.github.max_schall.appiary.data.repository.ColonyRepository
import io.github.max_schall.appiary.data.repository.FeedingRepository
import io.github.max_schall.appiary.data.repository.HarvestRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.data.repository.InspectionRepository
import io.github.max_schall.appiary.data.repository.MiteCheckRepository
import io.github.max_schall.appiary.data.repository.PhotoRepository
import io.github.max_schall.appiary.data.repository.QueenRecordRepository
import io.github.max_schall.appiary.data.repository.RecommendationRepository
import io.github.max_schall.appiary.data.repository.TreatmentRepository
import io.github.max_schall.appiary.data.repository.WeightRepository
import io.github.max_schall.appiary.domain.rules.rank
import io.github.max_schall.appiary.nfc.NfcController
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.model.RecommendationItem
import io.github.max_schall.appiary.ui.model.TimelineEntry
import io.github.max_schall.appiary.ui.model.TimelineKind
import io.github.max_schall.appiary.ui.util.UiFormat
import io.github.max_schall.appiary.util.TimeUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HiveDetailUiState(
    val hive: HiveEntity? = null,
    val nextActions: List<RecommendationItem> = emptyList(),
    val timeline: List<TimelineEntry> = emptyList(),
    val photos: List<PhotoAttachmentEntity> = emptyList(),
)

private data class HiveMeta(
    val hive: HiveEntity?,
    val recs: List<io.github.max_schall.appiary.data.entity.GeneratedRecommendationEntity>,
    val queens: List<io.github.max_schall.appiary.data.entity.QueenRecordEntity>,
    val photos: List<PhotoAttachmentEntity>,
    val colony: List<io.github.max_schall.appiary.data.entity.ColonyEventEntity>,
)

class HiveDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val hiveRepo: HiveRepository,
    private val recommendationRepo: RecommendationRepository,
    private val colonyRepo: ColonyRepository,
    private val weightRepo: WeightRepository,
    inspectionRepo: InspectionRepository,
    miteCheckRepo: MiteCheckRepository,
    treatmentRepo: TreatmentRepository,
    feedingRepo: FeedingRepository,
    harvestRepo: HarvestRepository,
    queenRecordRepo: QueenRecordRepository,
    photoRepo: PhotoRepository,
) : AndroidViewModel(application) {

    val hiveId: String = checkNotNull(savedStateHandle["hiveId"])
    private fun s(resId: Int, vararg args: Any) = getApplication<Application>().getString(resId, *args)

    private val metaFlow = combine(
        hiveRepo.observeHive(hiveId),
        recommendationRepo.observeActiveByHive(hiveId),
        queenRecordRepo.observeByHive(hiveId),
        photoRepo.observeByHive(hiveId),
        colonyRepo.observeForHive(hiveId),
    ) { hive, recs, queens, photos, colony -> HiveMeta(hive, recs, queens, photos, colony) }

    /** Active hives in the same apiary (excluding this one) — merge targets. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val mergeCandidates = hiveRepo.observeHive(hiveId)
        .flatMapLatest { hive ->
            if (hive == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else hiveRepo.observeByApiary(hive.apiaryId)
        }
        .map { hives -> hives.filter { it.id != hiveId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val eventsFlow = combine(
        inspectionRepo.observeByHive(hiveId),
        miteCheckRepo.observeByHive(hiveId),
        treatmentRepo.observeByHive(hiveId),
        feedingRepo.observeByHive(hiveId),
        harvestRepo.observeByHive(hiveId),
    ) { inspections, mites, treatments, feedings, harvests ->
        buildList {
            inspections.forEach {
                val summary = s(
                    R.string.timeline_inspection_summary,
                    s(it.queenSeen.labelRes()), s(it.broodPattern.labelRes()), s(it.strength.labelRes()),
                ) + if (it.swarmSigns || it.queenCells) s(R.string.timeline_swarm_suffix) else ""
                add(TimelineEntry(it.id, it.performedAt, TimelineKind.INSPECTION, s(R.string.timeline_inspection), summary))
            }
            mites.forEach {
                val detail = it.result?.let { r -> s(r.labelRes()) }
                    ?: it.mitesPerHundred?.let { p -> "%.1f%%".format(p) }
                    ?: s(R.string.timeline_logged)
                add(TimelineEntry(it.id, it.checkedAt, TimelineKind.MITE, s(R.string.timeline_mite, s(it.method.labelRes())), detail))
            }
            treatments.forEach {
                val end = it.endedAt?.let { e -> " – ${UiFormat.shortDate(e)}" } ?: s(R.string.timeline_treatment_ongoing)
                add(TimelineEntry(it.id, it.startedAt, TimelineKind.TREATMENT, s(it.type.labelRes()), "${UiFormat.shortDate(it.startedAt)}$end"))
            }
            feedings.forEach {
                val qty = it.quantity?.let { q -> " · ${q}${it.unit.orEmpty()}" } ?: ""
                add(TimelineEntry(it.id, it.fedAt, TimelineKind.FEEDING, s(R.string.timeline_feeding, s(it.feedType.labelRes())), "${s(R.string.timeline_fed)}$qty"))
            }
            harvests.forEach {
                val amt = it.amountKg?.let { kg -> "$kg kg" } ?: s(R.string.timeline_logged)
                add(TimelineEntry(it.id, it.harvestedAt, TimelineKind.HARVEST, s(R.string.timeline_harvest, s(it.product.labelRes())), amt))
            }
        }
    }

    private val weightFlow = weightRepo.observeByHive(hiveId).map { entries ->
        entries.map {
            TimelineEntry(
                it.id, it.recordedAt, TimelineKind.WEIGHT, s(R.string.timeline_weight),
                "%.1f kg".format(it.weightKg),
            )
        }
    }

    val uiState = combine(metaFlow, eventsFlow, weightFlow) { meta, events, weights ->
        val queenEntries = meta.queens.map {
            TimelineEntry(
                it.id, it.recordedAt, TimelineKind.QUEEN, s(R.string.timeline_queen, s(it.event.labelRes())),
                listOfNotNull(s(it.resultingStatus.labelRes()), it.markColor).joinToString(" · "),
            )
        }
        val colonyEntries = meta.colony.map { e ->
            val subject = e.hiveId == hiveId
            val name = e.relatedHiveName ?: s(R.string.pdf_none)
            val (title, summary) = when (e.type) {
                io.github.max_schall.appiary.domain.model.ColonyEventType.SPLIT ->
                    s(R.string.colony_split) to (if (subject) s(R.string.colony_split_from, name) else s(R.string.colony_split_off))
                io.github.max_schall.appiary.domain.model.ColonyEventType.SWARM_CAPTURE ->
                    s(R.string.colony_swarm) to s(R.string.colony_swarm_caught)
                io.github.max_schall.appiary.domain.model.ColonyEventType.MERGE ->
                    s(R.string.colony_merge) to (if (subject) s(R.string.colony_merge_in, name) else s(R.string.colony_merged_into))
            }
            TimelineEntry(e.id, e.occurredAt, TimelineKind.COLONY, title, summary)
        }
        HiveDetailUiState(
            hive = meta.hive,
            nextActions = meta.recs.sortedWith(compareBy({ it.urgencyBucket.rank }, { -it.urgencyScore }))
                .map { RecommendationItem(it, meta.hive?.name, null) },
            timeline = (events + queenEntries + colonyEntries + weights).sortedByDescending { it.timestamp },
            photos = meta.photos,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HiveDetailUiState())

    fun splitColony(daughterName: String, daughterTakesQueen: Boolean) = viewModelScope.launch {
        if (daughterName.isNotBlank()) colonyRepo.split(hiveId, daughterName.trim(), daughterTakesQueen)
    }

    fun mergeInto(targetId: String) = viewModelScope.launch {
        colonyRepo.merge(sourceId = hiveId, targetId = targetId)
    }

    fun logWeight(weightKg: Double) = viewModelScope.launch {
        val apiaryId = hiveRepo.getHive(hiveId)?.apiaryId ?: return@launch
        weightRepo.add(hiveId = hiveId, apiaryId = apiaryId, weightKg = weightKg)
    }

    fun complete(id: String) = viewModelScope.launch { recommendationRepo.complete(id) }
    fun dismiss(id: String) = viewModelScope.launch { recommendationRepo.dismiss(id) }
    fun snooze(id: String, days: Long = 3) =
        viewModelScope.launch { recommendationRepo.snooze(id, System.currentTimeMillis() + TimeUtil.days(days)) }

    /** Link the most recently scanned NFC tag to this hive (no-op if none). */
    fun linkNfcTag(onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val tag = NfcController.lastScannedTag.value
        val hive = hiveRepo.getHive(hiveId)
        if (tag != null && hive != null) {
            hiveRepo.save(hive.copy(nfcTagId = tag)); onResult(true)
        } else onResult(false)
    }
}
