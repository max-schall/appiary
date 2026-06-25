package io.github.max_schall.appiary.ui.navigation

/**
 * Routes for the logging flows. Each accepts an optional `hiveId` query arg so a
 * flow can be opened cold (from the quick-add FAB → hive picker) or pre-targeted
 * (from a hive's detail screen).
 */
object LogRoutes {
    const val INSPECTION = "log/inspection"
    const val MITE = "log/mite"
    const val TREATMENT = "log/treatment"
    const val FEEDING = "log/feeding"
    const val HARVEST = "log/harvest"
    const val NOTE = "log/note"

    /** All base routes paired with the route pattern that declares the optional arg. */
    val all = listOf(INSPECTION, MITE, TREATMENT, FEEDING, HARVEST, NOTE)

    fun pattern(base: String) = "$base?hiveId={hiveId}"

    fun forAction(action: QuickAddAction): String = when (action) {
        QuickAddAction.Inspection -> INSPECTION
        QuickAddAction.Feeding -> FEEDING
        QuickAddAction.MiteCheck -> MITE
        QuickAddAction.Treatment -> TREATMENT
        QuickAddAction.Harvest -> HARVEST
        QuickAddAction.Note -> NOTE
    }

    fun withHive(base: String, hiveId: String?): String =
        if (hiveId == null) base else "$base?hiveId=$hiveId"
}
