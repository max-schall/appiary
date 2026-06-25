package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.ColonyStrength
import io.github.max_schall.appiary.domain.model.FoodStores
import io.github.max_schall.appiary.domain.model.HiveStatus
import io.github.max_schall.appiary.domain.model.QueenStatus
import io.github.max_schall.appiary.domain.model.YesNoUnsure
import io.github.max_schall.appiary.domain.usecase.DeriveHiveState
import org.junit.Assert.assertEquals
import org.junit.Test

class DeriveHiveStateTest {

    @Test fun `seeing the queen means queenright`() {
        val i = Fixtures.inspection(queenSeen = YesNoUnsure.YES, eggsSeen = YesNoUnsure.NO)
        assertEquals(QueenStatus.QUEENRIGHT, DeriveHiveState.queenStatus(i))
    }

    @Test fun `seeing eggs implies queenright even if queen unseen`() {
        val i = Fixtures.inspection(queenSeen = YesNoUnsure.NO, eggsSeen = YesNoUnsure.YES)
        assertEquals(QueenStatus.QUEENRIGHT, DeriveHiveState.queenStatus(i))
    }

    @Test fun `no queen and no eggs means queenless`() {
        val i = Fixtures.inspection(queenSeen = YesNoUnsure.NO, eggsSeen = YesNoUnsure.NO)
        assertEquals(QueenStatus.QUEENLESS, DeriveHiveState.queenStatus(i))
    }

    @Test fun `unsure stays uncertain`() {
        val i = Fixtures.inspection(queenSeen = YesNoUnsure.UNSURE, eggsSeen = YesNoUnsure.UNSURE)
        assertEquals(QueenStatus.UNCERTAIN, DeriveHiveState.queenStatus(i))
    }

    @Test fun `apply rolls inspection into the hive snapshot`() {
        val hive = Fixtures.hive(
            queenStatus = QueenStatus.QUEENRIGHT, strength = ColonyStrength.STRONG,
            foodStores = FoodStores.STRONG,
        )
        val i = Fixtures.inspection(
            daysAgo = 1, queenSeen = YesNoUnsure.NO, eggsSeen = YesNoUnsure.NO,
            strength = ColonyStrength.WEAK, foodStores = FoodStores.LOW,
        )
        val updated = DeriveHiveState.apply(hive, i, Fixtures.NOW)
        assertEquals(QueenStatus.QUEENLESS, updated.queenStatus)
        assertEquals(HiveStatus.QUEENLESS, updated.status)
        assertEquals(ColonyStrength.WEAK, updated.strength)
        assertEquals(FoodStores.LOW, updated.foodStores)
        assertEquals(i.performedAt, updated.lastInspectionAt)
    }
}
