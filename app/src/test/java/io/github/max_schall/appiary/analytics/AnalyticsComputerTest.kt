package io.github.max_schall.appiary.analytics

import io.github.max_schall.appiary.data.entity.HarvestEventEntity
import io.github.max_schall.appiary.data.entity.InspectionEntity
import io.github.max_schall.appiary.data.entity.MiteCheckEntity
import io.github.max_schall.appiary.domain.analytics.AnalyticsComputer
import io.github.max_schall.appiary.domain.model.HarvestProduct
import io.github.max_schall.appiary.domain.model.MiteCheckMethod
import io.github.max_schall.appiary.domain.model.MiteResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Builds timestamps with the same default timezone the computer reads, so
 * year/month bucketing is deterministic regardless of where tests run.
 */
class AnalyticsComputerTest {

    private fun at(year: Int, month1to12: Int, day: Int = 15): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month1to12 - 1, day, 12, 0, 0)
        }.timeInMillis

    private val now = at(2025, 6)

    private fun harvest(year: Int, month: Int, kg: Double?, product: HarvestProduct = HarvestProduct.HONEY) =
        HarvestEventEntity(
            hiveId = "h1", apiaryId = "a1", harvestedAt = at(year, month),
            product = product, amountKg = kg, createdAt = 0, updatedAt = 0,
        )

    private fun inspection(year: Int, month: Int) = InspectionEntity(
        hiveId = "h1", apiaryId = "a1", performedAt = at(year, month),
        createdAt = 0, updatedAt = 0,
    )

    private fun mite(year: Int, month: Int, perHundred: Double?, result: MiteResult?) = MiteCheckEntity(
        hiveId = "h1", apiaryId = "a1", checkedAt = at(year, month),
        method = MiteCheckMethod.ALCOHOL_WASH, mitesPerHundred = perHundred, result = result,
        createdAt = 0, updatedAt = 0,
    )

    @Test
    fun `honey is summed per year and only counts honey with a known amount`() {
        val data = AnalyticsComputer.compute(
            harvests = listOf(
                harvest(2024, 7, 18.0),
                harvest(2025, 6, 12.0),
                harvest(2025, 8, 8.0),
                harvest(2025, 8, null),                       // no amount -> ignored
                harvest(2025, 8, 5.0, HarvestProduct.WAX),    // not honey -> ignored
            ),
            miteChecks = emptyList(),
            inspections = emptyList(),
            hivesTracked = 4,
            nowMs = now,
        )
        assertEquals(listOf(2024, 2025), data.honeyByYear.map { it.year })
        assertEquals(18.0, data.honeyByYear.first { it.year == 2024 }.kg, 0.001)
        assertEquals(20.0, data.honeyByYear.first { it.year == 2025 }.kg, 0.001)
        assertEquals(20.0, data.honeyThisYearKg, 0.001)
        assertEquals(38.0, data.honeyAllTimeKg, 0.001)
    }

    @Test
    fun `inspection cadence spans a fixed 12-month window and excludes older entries`() {
        val data = AnalyticsComputer.compute(
            harvests = emptyList(),
            miteChecks = emptyList(),
            inspections = listOf(
                inspection(2025, 6),
                inspection(2025, 6),
                inspection(2025, 4),
                inspection(2023, 1),   // older than 12 months -> excluded
            ),
            hivesTracked = 1,
            nowMs = now,
        )
        assertEquals(12, data.inspectionsByMonth.size)
        assertEquals(3, data.inspectionsTotal)
        val current = data.inspectionsByMonth.last()
        assertEquals(6, current.yearMonth.month)
        assertEquals(2, current.count)
    }

    @Test
    fun `varroa points drop unmeasured checks and average the current year`() {
        val data = AnalyticsComputer.compute(
            harvests = emptyList(),
            miteChecks = listOf(
                mite(2025, 5, 2.0, MiteResult.MODERATE),
                mite(2025, 6, 4.0, MiteResult.HIGH),
                mite(2025, 3, null, null),   // unmeasured -> dropped
                mite(2024, 8, 6.0, MiteResult.CRITICAL),
            ),
            inspections = emptyList(),
            hivesTracked = 1,
            nowMs = now,
        )
        assertEquals(3, data.varroaPoints.size)
        assertTrue(data.varroaPoints.zipWithNext().all { (a, b) -> a.timeMs <= b.timeMs })
        // Average uses this-year points only: (2.0 + 4.0) / 2.
        assertEquals(3.0, data.avgMiteLoad!!, 0.001)
    }

    @Test
    fun `empty input yields an empty, chartless dataset`() {
        val data = AnalyticsComputer.compute(emptyList(), emptyList(), emptyList(), 0, now)
        assertTrue(data.isEmpty)
        assertNull(data.avgMiteLoad)
    }
}
