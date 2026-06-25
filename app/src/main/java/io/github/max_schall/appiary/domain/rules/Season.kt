package io.github.max_schall.appiary.domain.rules

import io.github.max_schall.appiary.data.entity.SeasonalProfileEntity

/** Seasonal helpers. Month ranges may wrap the year end (southern hemisphere). */
object Season {

    /** True if [month] (1-12) falls within [start]..[end], inclusive, wrapping. */
    fun monthInRange(month: Int, start: Int, end: Int): Boolean =
        if (start <= end) month in start..end else month >= start || month <= end

    fun isActiveSeason(month: Int, profile: SeasonalProfileEntity?): Boolean {
        if (profile == null) return month in 3..9
        return monthInRange(month, profile.activeSeasonStartMonth, profile.activeSeasonEndMonth)
    }

    fun isHarvestWindow(month: Int, profile: SeasonalProfileEntity?): Boolean {
        if (profile == null) return month in 7..9
        return monthInRange(month, profile.harvestStartMonth, profile.harvestEndMonth)
    }

    /** The month immediately before harvest opens — when prep is advised. */
    fun isHarvestPrepMonth(month: Int, profile: SeasonalProfileEntity?): Boolean {
        val start = profile?.harvestStartMonth ?: 7
        val prep = if (start == 1) 12 else start - 1
        return month == prep
    }
}
