package io.github.max_schall.appiary.ui.i18n

import androidx.annotation.StringRes
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.BroodPattern
import io.github.max_schall.appiary.domain.model.ColonyStrength
import io.github.max_schall.appiary.domain.model.DiseaseConcern
import io.github.max_schall.appiary.domain.model.FeedType
import io.github.max_schall.appiary.domain.model.FoodStores
import io.github.max_schall.appiary.domain.model.HarvestProduct
import io.github.max_schall.appiary.domain.model.HiveStatus
import io.github.max_schall.appiary.domain.model.MiteCheckMethod
import io.github.max_schall.appiary.domain.model.MiteResult
import io.github.max_schall.appiary.domain.model.PestObservation
import io.github.max_schall.appiary.domain.model.QueenEventType
import io.github.max_schall.appiary.domain.model.QueenStatus
import io.github.max_schall.appiary.domain.model.Temperament
import io.github.max_schall.appiary.domain.model.TreatmentState
import io.github.max_schall.appiary.domain.model.TreatmentType
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.model.YesNoUnsure
import io.github.max_schall.appiary.domain.season.FlowStatus
import io.github.max_schall.appiary.domain.season.SeasonPhase
import io.github.max_schall.appiary.domain.season.WinterSeverity
import io.github.max_schall.appiary.ui.navigation.QuickAddAction
import io.github.max_schall.appiary.ui.navigation.TopDestination

/**
 * Maps domain enums to localized string resources. Returning a `@StringRes` id
 * (not a `@Composable` string) lets both Composables (`stringResource(...)`) and
 * ViewModels (`context.getString(...)`) resolve the same label.
 */
@StringRes fun io.github.max_schall.appiary.domain.model.HiveOrigin.labelRes(): Int = when (this) {
    io.github.max_schall.appiary.domain.model.HiveOrigin.UNKNOWN -> R.string.origin_unknown
    io.github.max_schall.appiary.domain.model.HiveOrigin.PACKAGE -> R.string.origin_package
    io.github.max_schall.appiary.domain.model.HiveOrigin.NUC -> R.string.origin_nuc
    io.github.max_schall.appiary.domain.model.HiveOrigin.SWARM -> R.string.origin_swarm
    io.github.max_schall.appiary.domain.model.HiveOrigin.SPLIT -> R.string.origin_split
}

@StringRes fun HiveStatus.labelRes(): Int = when (this) {
    HiveStatus.ACTIVE -> R.string.hivestatus_active
    HiveStatus.WEAK -> R.string.hivestatus_weak
    HiveStatus.QUEENLESS -> R.string.hivestatus_queenless
    HiveStatus.DEAD -> R.string.hivestatus_dead
}

@StringRes fun QueenStatus.labelRes(): Int = when (this) {
    QueenStatus.QUEENRIGHT -> R.string.queen_queenright
    QueenStatus.QUEENLESS -> R.string.queen_queenless
    QueenStatus.UNCERTAIN -> R.string.queen_uncertain
    QueenStatus.VIRGIN -> R.string.queen_virgin
}

@StringRes fun BroodPattern.labelRes(): Int = when (this) {
    BroodPattern.GOOD -> R.string.brood_good
    BroodPattern.UNEVEN -> R.string.brood_uneven
    BroodPattern.WEAK -> R.string.brood_weak
    BroodPattern.NONE -> R.string.brood_none
    BroodPattern.UNKNOWN -> R.string.brood_unknown
}

@StringRes fun ColonyStrength.labelRes(): Int = when (this) {
    ColonyStrength.WEAK -> R.string.strength_weak
    ColonyStrength.MODERATE -> R.string.strength_moderate
    ColonyStrength.STRONG -> R.string.strength_strong
    ColonyStrength.UNKNOWN -> R.string.strength_unknown
}

@StringRes fun Temperament.labelRes(): Int = when (this) {
    Temperament.CALM -> R.string.temperament_calm
    Temperament.NORMAL -> R.string.temperament_normal
    Temperament.DEFENSIVE -> R.string.temperament_defensive
    Temperament.UNKNOWN -> R.string.temperament_unknown
}

@StringRes fun FoodStores.labelRes(): Int = when (this) {
    FoodStores.LOW -> R.string.food_low
    FoodStores.OKAY -> R.string.food_okay
    FoodStores.STRONG -> R.string.food_strong
    FoodStores.UNKNOWN -> R.string.food_unknown
}

@StringRes fun YesNoUnsure.labelRes(): Int = when (this) {
    YesNoUnsure.YES -> R.string.yesno_yes
    YesNoUnsure.NO -> R.string.yesno_no
    YesNoUnsure.UNSURE -> R.string.yesno_unsure
}

@StringRes fun DiseaseConcern.labelRes(): Int = when (this) {
    DiseaseConcern.NONE -> R.string.disease_none
    DiseaseConcern.SUSPECTED -> R.string.disease_suspected
}

@StringRes fun PestObservation.labelRes(): Int = when (this) {
    PestObservation.NONE -> R.string.pest_none
    PestObservation.VARROA_SUSPECTED -> R.string.pest_varroa
    PestObservation.WASPS -> R.string.pest_wasps
    PestObservation.HORNETS -> R.string.pest_hornets
    PestObservation.OTHER -> R.string.pest_other
}

@StringRes fun TreatmentState.labelRes(): Int = when (this) {
    TreatmentState.NONE -> R.string.treatstate_none
    TreatmentState.IN_PROGRESS -> R.string.treatstate_in_progress
    TreatmentState.COMPLETED -> R.string.treatstate_completed
    TreatmentState.FOLLOW_UP_DUE -> R.string.treatstate_followup
}

@StringRes fun TreatmentType.labelRes(): Int = when (this) {
    TreatmentType.OXALIC_ACID -> R.string.treattype_oxalic
    TreatmentType.FORMIC_ACID -> R.string.treattype_formic
    TreatmentType.THYMOL -> R.string.treattype_thymol
    TreatmentType.AMITRAZ -> R.string.treattype_amitraz
    TreatmentType.APIVAR -> R.string.treattype_apivar
    TreatmentType.OTHER -> R.string.treattype_other
}

@StringRes fun MiteCheckMethod.labelRes(): Int = when (this) {
    MiteCheckMethod.ALCOHOL_WASH -> R.string.mitemethod_alcohol
    MiteCheckMethod.SUGAR_ROLL -> R.string.mitemethod_sugar
    MiteCheckMethod.STICKY_BOARD -> R.string.mitemethod_sticky
    MiteCheckMethod.CO2 -> R.string.mitemethod_co2
    MiteCheckMethod.VISUAL -> R.string.mitemethod_visual
}

@StringRes fun FeedType.labelRes(): Int = when (this) {
    FeedType.SYRUP_LIGHT -> R.string.feedtype_syrup_light
    FeedType.SYRUP_HEAVY -> R.string.feedtype_syrup_heavy
    FeedType.FONDANT -> R.string.feedtype_fondant
    FeedType.CANDY -> R.string.feedtype_candy
    FeedType.POLLEN_SUB -> R.string.feedtype_pollen
    FeedType.OTHER -> R.string.feedtype_other
}

@StringRes fun HarvestProduct.labelRes(): Int = when (this) {
    HarvestProduct.HONEY -> R.string.harvest_honey
    HarvestProduct.WAX -> R.string.harvest_wax
    HarvestProduct.PROPOLIS -> R.string.harvest_propolis
    HarvestProduct.POLLEN -> R.string.harvest_pollen
}

@StringRes fun QueenEventType.labelRes(): Int = when (this) {
    QueenEventType.SEEN -> R.string.qet_seen
    QueenEventType.MARKED -> R.string.qet_marked
    QueenEventType.REQUEENED -> R.string.qet_requeened
    QueenEventType.REPLACED -> R.string.qet_replaced
    QueenEventType.SUPERSEDED -> R.string.qet_superseded
    QueenEventType.FAILED -> R.string.qet_failed
    QueenEventType.LOST -> R.string.qet_lost
}

@StringRes fun MiteResult.labelRes(): Int = when (this) {
    MiteResult.LOW -> R.string.miteresult_low
    MiteResult.MODERATE -> R.string.miteresult_moderate
    MiteResult.HIGH -> R.string.miteresult_high
    MiteResult.CRITICAL -> R.string.miteresult_critical
}

@StringRes fun UrgencyBucket.labelRes(): Int = when (this) {
    UrgencyBucket.DO_NOW -> R.string.bucket_do_now
    UrgencyBucket.DUE_SOON -> R.string.bucket_due_soon
    UrgencyBucket.WATCHLIST -> R.string.bucket_watchlist
    UrgencyBucket.HEALTHY -> R.string.bucket_healthy
}

@StringRes fun SeasonPhase.labelRes(): Int = when (this) {
    SeasonPhase.WINTER -> R.string.phase_winter
    SeasonPhase.SPRING_BUILDUP -> R.string.phase_spring
    SeasonPhase.SWARM_AND_FLOW -> R.string.phase_swarm
    SeasonPhase.SUMMER_HARVEST -> R.string.phase_harvest
    SeasonPhase.AUTUMN_PREP -> R.string.phase_autumn
}

@StringRes fun FlowStatus.labelRes(): Int = when (this) {
    FlowStatus.NONE -> R.string.flow_none
    FlowStatus.IMMINENT -> R.string.flow_imminent
    FlowStatus.ACTIVE -> R.string.flow_active
    FlowStatus.DEARTH -> R.string.flow_dearth
}

@StringRes fun WinterSeverity.labelRes(): Int = when (this) {
    WinterSeverity.MILD -> R.string.winter_mild
    WinterSeverity.MODERATE -> R.string.winter_moderate
    WinterSeverity.HARSH -> R.string.winter_harsh
}

@StringRes fun TopDestination.labelRes(): Int = when (this) {
    TopDestination.Today -> R.string.nav_today
    TopDestination.Apiaries -> R.string.nav_apiaries
    TopDestination.Hives -> R.string.nav_hives
    TopDestination.Tasks -> R.string.nav_tasks
    TopDestination.Settings -> R.string.nav_settings
}

@StringRes fun QuickAddAction.labelRes(): Int = when (this) {
    QuickAddAction.Inspection -> R.string.qa_inspection
    QuickAddAction.Feeding -> R.string.qa_feeding
    QuickAddAction.MiteCheck -> R.string.qa_mite
    QuickAddAction.Treatment -> R.string.qa_treatment
    QuickAddAction.Harvest -> R.string.qa_harvest
    QuickAddAction.Note -> R.string.qa_note
}

@StringRes fun ActionType.labelRes(): Int = when (this) {
    ActionType.LOG_INSPECTION -> R.string.log_inspection
    ActionType.LOG_MITE_CHECK -> R.string.log_mite
    ActionType.LOG_TREATMENT -> R.string.log_treatment
    ActionType.LOG_FEEDING -> R.string.log_feeding
    ActionType.LOG_HARVEST -> R.string.log_harvest
    ActionType.OPEN_HIVE -> R.string.act_open_hive
    ActionType.COMPLETE_TASK -> R.string.action_done
    ActionType.REVIEW -> R.string.act_review
}
