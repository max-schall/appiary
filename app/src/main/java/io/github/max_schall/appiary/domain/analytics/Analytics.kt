package io.github.max_schall.appiary.domain.analytics

import io.github.max_schall.appiary.data.entity.HarvestEventEntity
import io.github.max_schall.appiary.data.entity.InspectionEntity
import io.github.max_schall.appiary.data.entity.MiteCheckEntity
import io.github.max_schall.appiary.domain.model.HarvestProduct
import io.github.max_schall.appiary.domain.model.MiteResult
import io.github.max_schall.appiary.util.CalendarUtil

/** Honey harvested in a single calendar year. */
data class YearTotal(val year: Int, val kg: Double)

/** Inspection count for one month on the rolling 12-month timeline. */
data class MonthCount(val yearMonth: CalendarUtil.YearMonth, val count: Int)

/** A single varroa measurement: infestation % at a point in time, with its banding. */
data class MitePoint(val timeMs: Long, val perHundred: Double, val result: MiteResult?)

/**
 * Everything the analytics screen renders, derived purely from stored events.
 * Locale-free (years/months are numbers; labels are formatted in the UI) and
 * deterministic so it can be unit-tested with a fixed [AnalyticsComputer.compute] clock.
 */
data class AnalyticsData(
    val honeyByYear: List<YearTotal>,
    val honeyThisYearKg: Double,
    val honeyAllTimeKg: Double,
    val inspectionsByMonth: List<MonthCount>,
    val inspectionsTotal: Int,
    val varroaPoints: List<MitePoint>,
    val avgMiteLoad: Double?,
    val hivesTracked: Int,
) {
    val hasHoney: Boolean get() = honeyByYear.isNotEmpty()
    val hasVarroa: Boolean get() = varroaPoints.isNotEmpty()
    val hasInspections: Boolean get() = inspectionsTotal > 0
    val isEmpty: Boolean get() = !hasHoney && !hasVarroa && !hasInspections
}

object AnalyticsComputer {

    /** Common varroa action thresholds (mites per 100 bees), drawn as reference lines. */
    const val MITE_CAUTION_THRESHOLD = 3.0
    const val MITE_ACTION_THRESHOLD = 5.0

    private const val MONTHS_WINDOW = 12
    private const val HONEY_YEARS_WINDOW = 6

    fun compute(
        harvests: List<HarvestEventEntity>,
        miteChecks: List<MiteCheckEntity>,
        inspections: List<InspectionEntity>,
        hivesTracked: Int,
        nowMs: Long,
    ): AnalyticsData {
        // --- Honey yield per year (honey only, amount known) ---
        val honeyEvents = harvests.filter { it.product == HarvestProduct.HONEY && it.amountKg != null }
        val byYear = honeyEvents
            .groupBy { CalendarUtil.yearOf(it.harvestedAt) }
            .map { (year, evts) -> YearTotal(year, evts.sumOf { it.amountKg ?: 0.0 }) }
            .sortedBy { it.year }
            .takeLast(HONEY_YEARS_WINDOW)
        val thisYear = CalendarUtil.yearOf(nowMs)
        val honeyThisYear = honeyEvents.filter { CalendarUtil.yearOf(it.harvestedAt) == thisYear }
            .sumOf { it.amountKg ?: 0.0 }
        val honeyAllTime = honeyEvents.sumOf { it.amountKg ?: 0.0 }

        // --- Inspection cadence: count per month across the trailing year (incl. zero months) ---
        val window = CalendarUtil.lastMonths(nowMs, MONTHS_WINDOW)
        val windowSet = window.toHashSet()
        val perMonth = inspections
            .map { CalendarUtil.yearMonthOf(it.performedAt) }
            .filter { it in windowSet }
            .groupingBy { it }.eachCount()
        val inspectionsByMonth = window.map { MonthCount(it, perMonth[it] ?: 0) }
        val inspectionsTotal = inspectionsByMonth.sumOf { it.count }

        // --- Varroa load over time (only checks with a computed infestation %) ---
        val varroaPoints = miteChecks
            .filter { it.mitesPerHundred != null }
            .map { MitePoint(it.checkedAt, it.mitesPerHundred!!, it.result) }
            .sortedBy { it.timeMs }
        val avgMiteLoad = varroaPoints
            .filter { CalendarUtil.yearOf(it.timeMs) == thisYear }
            .map { it.perHundred }
            .ifEmpty { varroaPoints.map { it.perHundred } }
            .takeIf { it.isNotEmpty() }
            ?.average()

        return AnalyticsData(
            honeyByYear = byYear,
            honeyThisYearKg = honeyThisYear,
            honeyAllTimeKg = honeyAllTime,
            inspectionsByMonth = inspectionsByMonth,
            inspectionsTotal = inspectionsTotal,
            varroaPoints = varroaPoints,
            avgMiteLoad = avgMiteLoad,
            hivesTracked = hivesTracked,
        )
    }
}
