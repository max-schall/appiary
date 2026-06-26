package io.github.max_schall.appiary.domain.usecase

import io.github.max_schall.appiary.domain.model.QueenMarkColor
import java.util.Calendar

/**
 * The international queen-marking colour convention: the colour is chosen by the
 * last digit of the year the queen was raised, on a five-year rotation. Suggesting
 * the right colour for the current year is a small but genuinely useful aid when
 * marking a new queen.
 */
object QueenMarking {

    /** Marking colour for the given calendar [year] (by its last digit). */
    fun colorForYear(year: Int): QueenMarkColor = when (year % 10) {
        1, 6 -> QueenMarkColor.WHITE
        2, 7 -> QueenMarkColor.YELLOW
        3, 8 -> QueenMarkColor.RED
        4, 9 -> QueenMarkColor.GREEN
        else -> QueenMarkColor.BLUE // 5, 0
    }

    /** Marking colour for the current year. */
    fun colorForNow(now: Long = System.currentTimeMillis()): QueenMarkColor {
        val year = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.YEAR)
        return colorForYear(year)
    }
}
