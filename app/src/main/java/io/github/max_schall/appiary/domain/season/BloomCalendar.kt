package io.github.max_schall.appiary.domain.season

/**
 * A forage plant's bloom window (Northern-hemisphere months 1–12) and its value
 * to bees. [majorFlow] marks surplus-producing flows (the ones that drive
 * supering/harvest), as opposed to build-up forage (e.g. willow, dandelion).
 */
data class ForageSource(
    val name: String,
    val startMonth: Int,
    val endMonth: Int,
    val nectar: Int,          // 0..3  (+, ++, +++)
    val majorFlow: Boolean = false,
    val honeydew: Boolean = false,
)

/** A region's forage calendar. Windows are stored Northern-hemisphere-relative. */
data class BloomCalendar(val key: String, val sources: List<ForageSource>)

/**
 * Curated calendars distilled from the literature (Timme's Trachtkalender for
 * Central Europe), plus generic fallbacks by climate group so any location maps
 * to a usable calendar. Note the deliberate late-summer dearth (August) between
 * the main flow and the autumn flow — the classic Central-European "Trachtlücke".
 */
object BloomCalendars {

    val EUROPE_TEMPERATE = BloomCalendar(
        key = "europe_temperate",
        sources = listOf(
            ForageSource("Hazel", 2, 2, 0),                 // pollen build-up
            ForageSource("Willow", 3, 3, 1),                // build-up
            ForageSource("Dandelion", 4, 4, 2),             // build-up/minor
            ForageSource("Oilseed rape", 4, 5, 3, majorFlow = true),
            ForageSource("Fruit blossom", 4, 5, 3, majorFlow = true),
            ForageSource("Black locust", 5, 6, 2, majorFlow = true),
            ForageSource("Raspberry/bramble", 6, 6, 3, majorFlow = true),
            ForageSource("Lime/linden", 6, 7, 2, majorFlow = true),
            ForageSource("White clover", 6, 7, 3, majorFlow = true),
            ForageSource("Honeydew (spruce/fir)", 6, 8, 2, honeydew = true),
            ForageSource("Goldenrod", 9, 9, 2, majorFlow = true),
            ForageSource("Himalayan balsam", 9, 9, 3, majorFlow = true),
        ),
    )

    val NORTH_AMERICA_TEMPERATE = BloomCalendar(
        key = "north_america_temperate",
        sources = listOf(
            ForageSource("Maple/willow", 3, 3, 1),
            ForageSource("Fruit/dandelion", 4, 5, 3, majorFlow = true),
            ForageSource("Black locust", 5, 5, 2, majorFlow = true),
            ForageSource("Clover", 6, 7, 3, majorFlow = true),
            ForageSource("Basswood/linden", 6, 7, 2, majorFlow = true),
            ForageSource("Goldenrod/aster", 9, 9, 3, majorFlow = true),
        ),
    )

    /** Generic fallback by climate group, using synthetic main-flow windows. */
    fun generic(group: KoppenGroup): BloomCalendar = when (group) {
        KoppenGroup.TROPICAL -> BloomCalendar(
            "generic_tropical",
            listOf(ForageSource("Year-round forage", 1, 12, 2, majorFlow = true)),
        )
        KoppenGroup.ARID -> BloomCalendar(
            "generic_arid",
            listOf(ForageSource("Brief spring flow", 3, 4, 2, majorFlow = true)),
        )
        KoppenGroup.CONTINENTAL -> BloomCalendar(
            "generic_continental",
            listOf(
                ForageSource("Spring flow", 5, 6, 3, majorFlow = true),
                ForageSource("Late flow", 7, 7, 2, majorFlow = true),
            ),
        )
        else -> BloomCalendar(
            "generic_temperate",
            listOf(
                ForageSource("Spring flow", 4, 5, 3, majorFlow = true),
                ForageSource("Early-summer flow", 6, 7, 3, majorFlow = true),
                ForageSource("Autumn flow", 9, 9, 2, majorFlow = true),
            ),
        )
    }
}
