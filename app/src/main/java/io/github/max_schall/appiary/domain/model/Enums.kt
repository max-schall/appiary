package io.github.max_schall.appiary.domain.model

import kotlinx.serialization.Serializable

/**
 * Shared domain enums. These are referenced directly by Room entities (via
 * [io.github.max_schall.appiary.data.db.Converters]) and by the rules engine, so there
 * is a single source of truth for structured field values. Each has a
 * human-readable [label] for UI use. @Serializable enables JSON backup.
 */

@Serializable
enum class HiveStatus(val label: String) {
    ACTIVE("Active"),
    WEAK("Weak"),
    QUEENLESS("Queenless"),
    DEAD("Dead"),
}

@Serializable
enum class QueenStatus(val label: String) {
    QUEENRIGHT("Queenright"),
    QUEENLESS("Queenless"),
    UNCERTAIN("Uncertain"),
    VIRGIN("Virgin"),
}

@Serializable
enum class BroodPattern(val label: String) {
    GOOD("Good"),
    UNEVEN("Uneven"),
    WEAK("Weak"),
    NONE("None"),
    UNKNOWN("Unknown"),
}

@Serializable
enum class ColonyStrength(val label: String) {
    WEAK("Weak"),
    MODERATE("Moderate"),
    STRONG("Strong"),
    UNKNOWN("Unknown"),
}

@Serializable
enum class Temperament(val label: String) {
    CALM("Calm"),
    NORMAL("Normal"),
    DEFENSIVE("Defensive"),
    UNKNOWN("Unknown"),
}

@Serializable
enum class FoodStores(val label: String) {
    LOW("Low"),
    OKAY("Okay"),
    STRONG("Strong"),
    UNKNOWN("Unknown"),
}

/** Tri-state used for observed-or-not fields in fast inspection entry. */
@Serializable
enum class YesNoUnsure(val label: String) {
    YES("Yes"),
    NO("No"),
    UNSURE("Unsure"),
}

@Serializable
enum class DiseaseConcern(val label: String) {
    NONE("None"),
    SUSPECTED("Suspected"),
}

@Serializable
enum class PestObservation(val label: String) {
    NONE("None"),
    VARROA_SUSPECTED("Varroa suspected"),
    WASPS("Wasps"),
    HORNETS("Hornets"),
    OTHER("Other"),
}

/** Treatment lifecycle state cached on the hive for fast list/rule access. */
@Serializable
enum class TreatmentState(val label: String) {
    NONE("None"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed"),
    FOLLOW_UP_DUE("Follow-up due"),
}

@Serializable
enum class TreatmentType(val label: String) {
    OXALIC_ACID("Oxalic acid"),
    FORMIC_ACID("Formic acid"),
    THYMOL("Thymol"),
    AMITRAZ("Amitraz"),
    APIVAR("Apivar"),
    OTHER("Other"),
}

@Serializable
enum class MiteCheckMethod(val label: String) {
    ALCOHOL_WASH("Alcohol wash"),
    SUGAR_ROLL("Sugar roll"),
    STICKY_BOARD("Sticky board"),
    CO2("CO₂ injector"),
    VISUAL("Visual"),
}

@Serializable
enum class MiteResult(val label: String) {
    LOW("Low"),
    MODERATE("Moderate"),
    HIGH("High"),
    CRITICAL("Critical"),
}

@Serializable
enum class FeedType(val label: String) {
    SYRUP_LIGHT("Syrup 1:1"),
    SYRUP_HEAVY("Syrup 2:1"),
    FONDANT("Fondant"),
    CANDY("Candy"),
    POLLEN_SUB("Pollen substitute"),
    OTHER("Other"),
}

@Serializable
enum class HarvestProduct(val label: String) {
    HONEY("Honey"),
    WAX("Wax"),
    PROPOLIS("Propolis"),
    POLLEN("Pollen"),
}

@Serializable
enum class QueenEventType(val label: String) {
    SEEN("Queen seen"),
    MARKED("Marked"),
    REQUEENED("Requeened"),
    REPLACED("Replaced"),
    SUPERSEDED("Superseded"),
    FAILED("Failed"),
    LOST("Lost"),
}

@Serializable
enum class TaskStatus(val label: String) {
    OPEN("Open"),
    DONE("Done"),
    SNOOZED("Snoozed"),
    DISMISSED("Dismissed"),
}

@Serializable
enum class RecommendationCategory(val label: String) {
    INSPECTION("Inspection"),
    QUEEN("Queen"),
    SWARM("Swarm risk"),
    FEEDING("Feeding"),
    MITE_CHECK("Mite check"),
    TREATMENT("Treatment"),
    COLONY_HEALTH("Colony health"),
    HARVEST("Harvest"),
    MANUAL("Manual task"),
    SEASONAL("Seasonal"),
    NECTAR_FLOW("Nectar flow"),
    WEATHER("Weather"),
    COMPLIANCE("Record-keeping"),
}

/** The four buckets the Today screen sorts recommendations into. */
@Serializable
enum class UrgencyBucket(val label: String) {
    DO_NOW("Do now"),
    DUE_SOON("Due soon"),
    WATCHLIST("Watchlist"),
    HEALTHY("Healthy"),
}

@Serializable
enum class ActionType(val label: String) {
    LOG_INSPECTION("Log inspection"),
    LOG_MITE_CHECK("Log mite check"),
    LOG_TREATMENT("Log treatment"),
    LOG_FEEDING("Log feeding"),
    LOG_HARVEST("Log harvest"),
    OPEN_HIVE("Open hive"),
    COMPLETE_TASK("Mark done"),
    REVIEW("Review"),
}

@Serializable
enum class RecommendationStatus {
    ACTIVE, SNOOZED, COMPLETED, DISMISSED,
}

@Serializable
enum class ReminderType(val label: String) {
    DAILY_SUMMARY("Daily summary"),
    URGENT_ONLY("Urgent only"),
    POST_TREATMENT("Post-treatment check"),
}

@Serializable
enum class Hemisphere { NORTHERN, SOUTHERN }

/** How a colony came to exist — drives lineage display and the colony timeline. */
@Serializable
enum class HiveOrigin(val label: String) {
    UNKNOWN("Unknown"),
    PACKAGE("Package"),
    NUC("Nucleus"),
    SWARM("Captured swarm"),
    SPLIT("Split"),
}

/** A structural colony operation recorded in the colony_events log. */
@Serializable
enum class ColonyEventType(val label: String) {
    SPLIT("Split"),
    SWARM_CAPTURE("Swarm capture"),
    MERGE("Merge"),
}
