package io.github.max_schall.appiary.queen

import io.github.max_schall.appiary.domain.model.QueenMarkColor
import io.github.max_schall.appiary.domain.usecase.QueenMarking
import org.junit.Assert.assertEquals
import org.junit.Test

class QueenMarkingTest {

    @Test
    fun `colour follows the international five-year rotation`() {
        // White 1/6, Yellow 2/7, Red 3/8, Green 4/9, Blue 5/0.
        assertEquals(QueenMarkColor.WHITE, QueenMarking.colorForYear(2021))
        assertEquals(QueenMarkColor.YELLOW, QueenMarking.colorForYear(2022))
        assertEquals(QueenMarkColor.RED, QueenMarking.colorForYear(2023))
        assertEquals(QueenMarkColor.GREEN, QueenMarking.colorForYear(2024))
        assertEquals(QueenMarkColor.BLUE, QueenMarking.colorForYear(2025))
        assertEquals(QueenMarkColor.WHITE, QueenMarking.colorForYear(2026))
        assertEquals(QueenMarkColor.BLUE, QueenMarking.colorForYear(2020))
    }
}
