package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.data.entity.InspectionEntity
import io.github.max_schall.appiary.data.entity.ManualTaskEntity
import io.github.max_schall.appiary.data.entity.MiteCheckEntity
import io.github.max_schall.appiary.data.entity.SeasonalProfileEntity
import io.github.max_schall.appiary.data.entity.TreatmentEventEntity
import io.github.max_schall.appiary.domain.model.BroodPattern
import io.github.max_schall.appiary.domain.model.ColonyStrength
import io.github.max_schall.appiary.domain.model.FoodStores
import io.github.max_schall.appiary.domain.model.HiveStatus
import io.github.max_schall.appiary.domain.model.MiteCheckMethod
import io.github.max_schall.appiary.domain.model.MiteResult
import io.github.max_schall.appiary.domain.model.QueenStatus
import io.github.max_schall.appiary.domain.model.TaskStatus
import io.github.max_schall.appiary.domain.model.Temperament
import io.github.max_schall.appiary.domain.model.TreatmentState
import io.github.max_schall.appiary.domain.model.TreatmentType
import io.github.max_schall.appiary.domain.model.YesNoUnsure
import io.github.max_schall.appiary.domain.rules.HiveContext
import io.github.max_schall.appiary.util.TimeUtil.days

/**
 * Deterministic test fixtures. NOW is fixed; rules read the calendar month from
 * [HiveContext.currentMonth] (default 6 = active season), so tests never depend
 * on the real clock or timezone.
 */
object Fixtures {
    const val NOW = 1_750_000_000_000L // arbitrary fixed instant
    fun ago(d: Long) = NOW - days(d)
    fun ahead(d: Long) = NOW + days(d)

    fun hive(
        id: String = "h1",
        apiaryId: String = "a1",
        name: String = "Test hive",
        status: HiveStatus = HiveStatus.ACTIVE,
        queenStatus: QueenStatus = QueenStatus.QUEENRIGHT,
        broodPattern: BroodPattern = BroodPattern.GOOD,
        strength: ColonyStrength = ColonyStrength.STRONG,
        temperament: Temperament = Temperament.CALM,
        foodStores: FoodStores = FoodStores.OKAY,
        treatmentState: TreatmentState = TreatmentState.NONE,
        lastInspectionAt: Long? = ago(2),
        lastMiteCheckAt: Long? = ago(5),
        lastTreatmentEndedAt: Long? = null,
        postTreatmentCheckDueAt: Long? = null,
        installedAt: Long? = ago(300),
    ) = HiveEntity(
        id = id, apiaryId = apiaryId, name = name, status = status, queenStatus = queenStatus,
        broodPattern = broodPattern, strength = strength, temperament = temperament,
        foodStores = foodStores, treatmentState = treatmentState, lastInspectionAt = lastInspectionAt,
        lastMiteCheckAt = lastMiteCheckAt, lastTreatmentEndedAt = lastTreatmentEndedAt,
        postTreatmentCheckDueAt = postTreatmentCheckDueAt, installedAt = installedAt,
        createdAt = ago(300), updatedAt = NOW,
    )

    fun inspection(
        id: String = "i1",
        hiveId: String = "h1",
        apiaryId: String = "a1",
        daysAgo: Long = 0,
        queenSeen: YesNoUnsure = YesNoUnsure.YES,
        eggsSeen: YesNoUnsure = YesNoUnsure.YES,
        broodPattern: BroodPattern = BroodPattern.GOOD,
        strength: ColonyStrength = ColonyStrength.STRONG,
        temperament: Temperament = Temperament.CALM,
        foodStores: FoodStores = FoodStores.OKAY,
        swarmSigns: Boolean = false,
        queenCells: Boolean = false,
    ) = InspectionEntity(
        id = id, hiveId = hiveId, apiaryId = apiaryId, performedAt = ago(daysAgo),
        queenSeen = queenSeen, eggsSeen = eggsSeen, broodPattern = broodPattern, strength = strength,
        temperament = temperament, foodStores = foodStores, swarmSigns = swarmSigns,
        queenCells = queenCells, createdAt = ago(daysAgo), updatedAt = ago(daysAgo),
    )

    fun miteCheck(
        hiveId: String = "h1",
        daysAgo: Long = 5,
        result: MiteResult? = MiteResult.LOW,
    ) = MiteCheckEntity(
        id = "m1", hiveId = hiveId, apiaryId = "a1", checkedAt = ago(daysAgo),
        method = MiteCheckMethod.ALCOHOL_WASH, result = result,
        createdAt = ago(daysAgo), updatedAt = ago(daysAgo),
    )

    fun treatment(hiveId: String = "h1", endedDaysAgo: Long = 5) = TreatmentEventEntity(
        id = "t1", hiveId = hiveId, apiaryId = "a1", type = TreatmentType.FORMIC_ACID,
        startedAt = ago(endedDaysAgo + 14), endedAt = ago(endedDaysAgo),
        createdAt = ago(endedDaysAgo + 14), updatedAt = ago(endedDaysAgo),
    )

    fun task(
        id: String = "task1",
        title: String = "Do the thing",
        dueAt: Long? = ago(2),
        status: TaskStatus = TaskStatus.OPEN,
        hiveId: String? = null,
        apiaryId: String? = "a1",
    ) = ManualTaskEntity(
        id = id, title = title, hiveId = hiveId, apiaryId = apiaryId, dueAt = dueAt,
        status = status, createdAt = ago(10), updatedAt = ago(10),
    )

    fun profile() = SeasonalProfileEntity(
        id = "p1", name = "Test", createdAt = NOW, updatedAt = NOW, selected = true,
    )

    fun ctx(
        hive: HiveEntity,
        inspections: List<InspectionEntity> = emptyList(),
        miteCheck: MiteCheckEntity? = null,
        treatment: TreatmentEventEntity? = null,
        month: Int = 6,
        profile: SeasonalProfileEntity? = null,
    ) = HiveContext(
        hive = hive,
        recentInspections = inspections,
        latestMiteCheck = miteCheck,
        latestTreatment = treatment,
        now = NOW,
        currentMonth = month,
        seasonal = profile,
    )
}
