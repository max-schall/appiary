package io.github.max_schall.appiary.data.repository

import io.github.max_schall.appiary.data.dao.HarvestDao
import io.github.max_schall.appiary.data.dao.HiveDao
import io.github.max_schall.appiary.data.dao.InspectionDao
import io.github.max_schall.appiary.data.dao.MiteCheckDao
import io.github.max_schall.appiary.domain.analytics.AnalyticsComputer
import io.github.max_schall.appiary.domain.analytics.AnalyticsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Read-only aggregation over stored events for the Insights screen. Combines the
 * raw event streams and runs the pure [AnalyticsComputer]; no writes, no schema.
 */
class AnalyticsRepository(
    private val harvestDao: HarvestDao,
    private val miteCheckDao: MiteCheckDao,
    private val inspectionDao: InspectionDao,
    private val hiveDao: HiveDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observe(): Flow<AnalyticsData> = combine(
        harvestDao.observeAll(),
        miteCheckDao.observeAll(),
        inspectionDao.observeAll(),
        hiveDao.observeAll(),
    ) { harvests, miteChecks, inspections, hives ->
        AnalyticsComputer.compute(
            harvests = harvests,
            miteChecks = miteChecks,
            inspections = inspections,
            hivesTracked = hives.size,
            nowMs = clock(),
        )
    }
}
