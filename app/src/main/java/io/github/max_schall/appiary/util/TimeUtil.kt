package io.github.max_schall.appiary.util

import java.util.concurrent.TimeUnit

/** Small time helpers. Timestamps across the app are epoch milliseconds (UTC). */
object TimeUtil {
    fun days(n: Long): Long = TimeUnit.DAYS.toMillis(n)
    fun hours(n: Long): Long = TimeUnit.HOURS.toMillis(n)

    /** Whole days between two epoch-millis instants (floored, can be negative). */
    fun daysBetween(fromMillis: Long, toMillis: Long): Long =
        TimeUnit.MILLISECONDS.toDays(toMillis - fromMillis)
}
