package io.github.max_schall.appiary.ui.util

import io.github.max_schall.appiary.util.AppLocale
import io.github.max_schall.appiary.util.TimeUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Locale-aware display formatting (English/German) for the UI layer. */
object UiFormat {
    private fun isGerman() = AppLocale.effectiveLanguageCode() == "de"
    private fun locale() = if (isGerman()) Locale.GERMAN else Locale.ENGLISH

    fun shortDate(epochMillis: Long): String =
        SimpleDateFormat("d MMM", locale()).format(Date(epochMillis))

    fun fullDate(epochMillis: Long): String =
        SimpleDateFormat("d MMM yyyy", locale()).format(Date(epochMillis))

    /** Localized full month name for month 1–12. */
    fun monthName(month: Int): String =
        java.text.DateFormatSymbols(locale()).months.getOrNull(month - 1)?.takeIf { it.isNotBlank() }
            ?: month.toString()

    /** "today" / "3 days ago" / "in 2 days" (and German equivalents). */
    fun relativeDays(thenMillis: Long, now: Long = System.currentTimeMillis()): String {
        val days = TimeUtil.daysBetween(thenMillis, now)
        return if (isGerman()) {
            when {
                days == 0L -> "heute"
                days == 1L -> "gestern"
                days > 1L -> "vor $days Tagen"
                days == -1L -> "morgen"
                else -> "in ${-days} Tagen"
            }
        } else {
            when {
                days == 0L -> "today"
                days == 1L -> "yesterday"
                days > 1L -> "$days days ago"
                days == -1L -> "tomorrow"
                else -> "in ${-days} days"
            }
        }
    }

    fun relativeOrNever(thenMillis: Long?, now: Long = System.currentTimeMillis()): String =
        thenMillis?.let { relativeDays(it, now) } ?: if (isGerman()) "nie" else "never"
}
