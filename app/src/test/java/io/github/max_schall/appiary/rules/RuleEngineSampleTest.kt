package io.github.max_schall.appiary.rules

import io.github.max_schall.appiary.domain.model.QueenStatus
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.model.YesNoUnsure
import io.github.max_schall.appiary.domain.rules.EvaluationInput
import io.github.max_schall.appiary.domain.rules.RuleEngine
import io.github.max_schall.appiary.domain.rules.rank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** End-to-end checks that representative hives produce the expected output. */
class RuleEngineSampleTest {
    private val engine = RuleEngine()

    private val swarmHive = Fixtures.ctx(
        Fixtures.hive(id = "swarm", name = "Willow", lastInspectionAt = Fixtures.ago(3)),
        inspections = listOf(Fixtures.inspection(daysAgo = 3, queenCells = true)),
    )
    private val overdueQueenHive = Fixtures.ctx(
        Fixtures.hive(id = "maple", name = "Maple", queenStatus = QueenStatus.UNCERTAIN, lastInspectionAt = Fixtures.ago(20)),
        inspections = listOf(Fixtures.inspection(daysAgo = 20, queenSeen = YesNoUnsure.NO, eggsSeen = YesNoUnsure.UNSURE)),
    )
    private val healthyHive = Fixtures.ctx(
        Fixtures.hive(id = "linden", name = "Linden", lastInspectionAt = Fixtures.ago(3), lastMiteCheckAt = Fixtures.ago(7)),
        inspections = listOf(Fixtures.inspection(daysAgo = 3)),
    )

    @Test fun `swarm hive yields a do-now swarm recommendation`() {
        val recs = engine.evaluateHive(swarmHive)
        val swarm = recs.single { it.category == RecommendationCategory.SWARM }
        assertEquals(UrgencyBucket.DO_NOW, swarm.urgencyBucket)
    }

    @Test fun `overdue uncertain-queen hive yields both inspection and queen recs`() {
        val cats = engine.evaluateHive(overdueQueenHive).map { it.category }.toSet()
        assertTrue(cats.contains(RecommendationCategory.INSPECTION))
        assertTrue(cats.contains(RecommendationCategory.QUEEN))
    }

    @Test fun `healthy hive yields no recommendations`() {
        assertTrue(engine.evaluateHive(healthyHive).isEmpty())
    }

    @Test fun `full run sorts most-urgent first and includes overdue tasks`() {
        val input = EvaluationInput(
            hiveContexts = listOf(healthyHive, swarmHive, overdueQueenHive),
            openTasks = listOf(Fixtures.task(id = "t1", title = "Fix lid", dueAt = Fixtures.ago(2))),
            now = Fixtures.NOW,
        )
        val recs = engine.evaluate(input)

        assertTrue(recs.isNotEmpty())
        // Sorted: first item is the most urgent (Do now), last is least.
        assertEquals(UrgencyBucket.DO_NOW, recs.first().urgencyBucket)
        recs.zipWithNext { a, b ->
            assertTrue("not sorted: ${a.urgencyBucket} before ${b.urgencyBucket}", a.urgencyBucket.rank <= b.urgencyBucket.rank)
        }
        // The overdue manual task is represented.
        assertTrue(recs.any { it.category == RecommendationCategory.MANUAL && it.title == "Fix lid" })
        // Healthy hive contributed nothing.
        assertTrue(recs.none { it.hiveId == "linden" })
    }

    @Test fun `engine is deterministic`() {
        val input = EvaluationInput(
            hiveContexts = listOf(swarmHive, overdueQueenHive),
            openTasks = emptyList(),
            now = Fixtures.NOW,
        )
        assertEquals(engine.evaluate(input), engine.evaluate(input))
    }
}
