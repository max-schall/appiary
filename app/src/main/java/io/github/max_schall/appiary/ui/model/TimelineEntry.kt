package io.github.max_schall.appiary.ui.model

/** A single chronological event in a hive's history (unified timeline). */
data class TimelineEntry(
    val id: String,
    val timestamp: Long,
    val kind: TimelineKind,
    val title: String,
    val summary: String,
)

enum class TimelineKind { INSPECTION, MITE, TREATMENT, FEEDING, HARVEST, QUEEN, COLONY, WEIGHT }
