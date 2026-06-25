package io.github.max_schall.appiary.domain.season

import io.github.max_schall.appiary.domain.model.Hemisphere

/** Top-level Köppen climate group — what actually drives calendar/region selection. */
enum class KoppenGroup { TROPICAL, ARID, TEMPERATE, CONTINENTAL, POLAR }

/** Coarse winter severity derived from the hardiness zone, for overwintering advice. */
enum class WinterSeverity { MILD, MODERATE, HARSH }

/**
 * A location's derived climate descriptor. [code] is the full Köppen code (e.g. "Cfb")
 * for display; [group] and [winterSeverity] drive logic. [hardinessZone] is a
 * USDA-style 1–13 zone from the annual extreme-minimum temperature.
 */
data class ClimateProfile(
    val code: String,
    val group: KoppenGroup,
    val hardinessZone: Int,
    val winterSeverity: WinterSeverity,
    val hemisphere: Hemisphere,
)
