package io.github.max_schall.appiary.util

import java.util.Calendar

/** Calendar-month extraction kept here (java.time needs desugaring below API 26). */
object CalendarUtil {
    /** Month of [epochMillis] in the device's default timezone, 1-12. */
    fun monthOf(epochMillis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = epochMillis }.get(Calendar.MONTH) + 1

    /** Calendar year of [epochMillis] in the device's default timezone. */
    fun yearOf(epochMillis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = epochMillis }.get(Calendar.YEAR)

    /** A year+month bucket (month is 1-12), used to group events on a timeline. */
    data class YearMonth(val year: Int, val month: Int) : Comparable<YearMonth> {
        val ordinal: Int get() = year * 12 + (month - 1)
        override fun compareTo(other: YearMonth): Int = ordinal.compareTo(other.ordinal)
    }

    fun yearMonthOf(epochMillis: Long): YearMonth =
        Calendar.getInstance().apply { timeInMillis = epochMillis }.let {
            YearMonth(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1)
        }

    /** The [count] consecutive months ending at (and including) the month of [nowMillis]. */
    fun lastMonths(nowMillis: Long, count: Int): List<YearMonth> {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val end = YearMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        return (count - 1 downTo 0).map { back ->
            val ord = end.ordinal - back
            YearMonth(ord / 12, ord % 12 + 1)
        }
    }
}
