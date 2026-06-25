package io.github.max_schall.appiary.domain.usecase

import io.github.max_schall.appiary.data.entity.HiveEntity
import io.github.max_schall.appiary.data.entity.InspectionEntity
import io.github.max_schall.appiary.domain.model.ColonyStrength
import io.github.max_schall.appiary.domain.model.HiveStatus
import io.github.max_schall.appiary.domain.model.QueenStatus
import io.github.max_schall.appiary.domain.model.YesNoUnsure

/**
 * Pure derivation of a hive's current cached state from a freshly logged
 * inspection. Kept side-effect-free so it can be unit tested and reused by both
 * the repository (persistence) and the rules engine if needed.
 */
object DeriveHiveState {

    /** Infer queen status from what was actually observed during the visit. */
    fun queenStatus(inspection: InspectionEntity): QueenStatus = when {
        inspection.queenSeen == YesNoUnsure.YES || inspection.eggsSeen == YesNoUnsure.YES ->
            QueenStatus.QUEENRIGHT
        inspection.queenSeen == YesNoUnsure.NO && inspection.eggsSeen == YesNoUnsure.NO ->
            QueenStatus.QUEENLESS
        else -> QueenStatus.UNCERTAIN
    }

    /** Roll the inspection's structured fields into the hive's cached snapshot. */
    fun apply(current: HiveEntity, inspection: InspectionEntity, now: Long): HiveEntity {
        val queen = queenStatus(inspection)
        val status = when {
            queen == QueenStatus.QUEENLESS -> HiveStatus.QUEENLESS
            inspection.strength == ColonyStrength.WEAK -> HiveStatus.WEAK
            current.status == HiveStatus.DEAD -> HiveStatus.DEAD
            else -> HiveStatus.ACTIVE
        }
        return current.copy(
            status = status,
            queenStatus = queen,
            broodPattern = inspection.broodPattern,
            strength = inspection.strength,
            temperament = inspection.temperament,
            foodStores = inspection.foodStores,
            lastInspectionAt = inspection.performedAt,
            updatedAt = now,
        )
    }
}
