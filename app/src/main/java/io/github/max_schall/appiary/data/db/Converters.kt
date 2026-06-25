package io.github.max_schall.appiary.data.db

import androidx.room.TypeConverter
import io.github.max_schall.appiary.domain.model.ActionType
import io.github.max_schall.appiary.domain.model.BroodPattern
import io.github.max_schall.appiary.domain.model.ColonyStrength
import io.github.max_schall.appiary.domain.model.DiseaseConcern
import io.github.max_schall.appiary.domain.model.FeedType
import io.github.max_schall.appiary.domain.model.FoodStores
import io.github.max_schall.appiary.domain.model.HarvestProduct
import io.github.max_schall.appiary.domain.model.Hemisphere
import io.github.max_schall.appiary.domain.model.HiveStatus
import io.github.max_schall.appiary.domain.model.MiteCheckMethod
import io.github.max_schall.appiary.domain.model.MiteResult
import io.github.max_schall.appiary.domain.model.PestObservation
import io.github.max_schall.appiary.domain.model.QueenEventType
import io.github.max_schall.appiary.domain.model.QueenStatus
import io.github.max_schall.appiary.domain.model.RecommendationCategory
import io.github.max_schall.appiary.domain.model.RecommendationStatus
import io.github.max_schall.appiary.domain.model.ReminderType
import io.github.max_schall.appiary.domain.model.Temperament
import io.github.max_schall.appiary.domain.model.TreatmentState
import io.github.max_schall.appiary.domain.model.TreatmentType
import io.github.max_schall.appiary.domain.model.UrgencyBucket
import io.github.max_schall.appiary.domain.model.YesNoUnsure

/**
 * Enum <-> String converters. Enums are stored by [Enum.name] for stable,
 * human-readable, export-friendly values. Room skips the converter for null
 * columns, so non-null converters cover nullable fields too.
 */
class Converters {
    @TypeConverter fun fromHiveStatus(v: HiveStatus) = v.name
    @TypeConverter fun toHiveStatus(v: String) = enumValueOf<HiveStatus>(v)

    @TypeConverter fun fromQueenStatus(v: QueenStatus) = v.name
    @TypeConverter fun toQueenStatus(v: String) = enumValueOf<QueenStatus>(v)

    @TypeConverter fun fromBroodPattern(v: BroodPattern) = v.name
    @TypeConverter fun toBroodPattern(v: String) = enumValueOf<BroodPattern>(v)

    @TypeConverter fun fromColonyStrength(v: ColonyStrength) = v.name
    @TypeConverter fun toColonyStrength(v: String) = enumValueOf<ColonyStrength>(v)

    @TypeConverter fun fromTemperament(v: Temperament) = v.name
    @TypeConverter fun toTemperament(v: String) = enumValueOf<Temperament>(v)

    @TypeConverter fun fromFoodStores(v: FoodStores) = v.name
    @TypeConverter fun toFoodStores(v: String) = enumValueOf<FoodStores>(v)

    @TypeConverter fun fromYesNoUnsure(v: YesNoUnsure) = v.name
    @TypeConverter fun toYesNoUnsure(v: String) = enumValueOf<YesNoUnsure>(v)

    @TypeConverter fun fromDiseaseConcern(v: DiseaseConcern) = v.name
    @TypeConverter fun toDiseaseConcern(v: String) = enumValueOf<DiseaseConcern>(v)

    @TypeConverter fun fromPestObservation(v: PestObservation) = v.name
    @TypeConverter fun toPestObservation(v: String) = enumValueOf<PestObservation>(v)

    @TypeConverter fun fromTreatmentState(v: TreatmentState) = v.name
    @TypeConverter fun toTreatmentState(v: String) = enumValueOf<TreatmentState>(v)

    @TypeConverter fun fromTreatmentType(v: TreatmentType) = v.name
    @TypeConverter fun toTreatmentType(v: String) = enumValueOf<TreatmentType>(v)

    @TypeConverter fun fromMiteCheckMethod(v: MiteCheckMethod) = v.name
    @TypeConverter fun toMiteCheckMethod(v: String) = enumValueOf<MiteCheckMethod>(v)

    @TypeConverter fun fromMiteResult(v: MiteResult) = v.name
    @TypeConverter fun toMiteResult(v: String) = enumValueOf<MiteResult>(v)

    @TypeConverter fun fromFeedType(v: FeedType) = v.name
    @TypeConverter fun toFeedType(v: String) = enumValueOf<FeedType>(v)

    @TypeConverter fun fromHarvestProduct(v: HarvestProduct) = v.name
    @TypeConverter fun toHarvestProduct(v: String) = enumValueOf<HarvestProduct>(v)

    @TypeConverter fun fromQueenEventType(v: QueenEventType) = v.name
    @TypeConverter fun toQueenEventType(v: String) = enumValueOf<QueenEventType>(v)

    @TypeConverter fun fromTaskStatus(v: io.github.max_schall.appiary.domain.model.TaskStatus) = v.name
    @TypeConverter fun toTaskStatus(v: String) = enumValueOf<io.github.max_schall.appiary.domain.model.TaskStatus>(v)

    @TypeConverter fun fromRecommendationCategory(v: RecommendationCategory) = v.name
    @TypeConverter fun toRecommendationCategory(v: String) = enumValueOf<RecommendationCategory>(v)

    @TypeConverter fun fromUrgencyBucket(v: UrgencyBucket) = v.name
    @TypeConverter fun toUrgencyBucket(v: String) = enumValueOf<UrgencyBucket>(v)

    @TypeConverter fun fromActionType(v: ActionType) = v.name
    @TypeConverter fun toActionType(v: String) = enumValueOf<ActionType>(v)

    @TypeConverter fun fromRecommendationStatus(v: RecommendationStatus) = v.name
    @TypeConverter fun toRecommendationStatus(v: String) = enumValueOf<RecommendationStatus>(v)

    @TypeConverter fun fromReminderType(v: ReminderType) = v.name
    @TypeConverter fun toReminderType(v: String) = enumValueOf<ReminderType>(v)

    @TypeConverter fun fromHemisphere(v: Hemisphere) = v.name
    @TypeConverter fun toHemisphere(v: String) = enumValueOf<Hemisphere>(v)

    @TypeConverter fun fromHiveOrigin(v: io.github.max_schall.appiary.domain.model.HiveOrigin) = v.name
    @TypeConverter fun toHiveOrigin(v: String) = enumValueOf<io.github.max_schall.appiary.domain.model.HiveOrigin>(v)

    @TypeConverter fun fromColonyEventType(v: io.github.max_schall.appiary.domain.model.ColonyEventType) = v.name
    @TypeConverter fun toColonyEventType(v: String) = enumValueOf<io.github.max_schall.appiary.domain.model.ColonyEventType>(v)
}
