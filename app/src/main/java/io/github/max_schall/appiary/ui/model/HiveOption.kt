package io.github.max_schall.appiary.ui.model

/** A hive choice in logging-flow pickers, labelled with its apiary. */
data class HiveOption(
    val id: String,
    val hiveName: String,
    val apiaryId: String,
    val apiaryName: String?,
) {
    val label: String get() = listOfNotNull(apiaryName, hiveName).joinToString(" · ")
}
