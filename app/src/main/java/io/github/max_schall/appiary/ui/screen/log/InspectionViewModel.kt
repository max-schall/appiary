package io.github.max_schall.appiary.ui.screen.log

import androidx.lifecycle.SavedStateHandle
import io.github.max_schall.appiary.data.entity.InspectionEntity
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import io.github.max_schall.appiary.data.repository.HiveRepository
import io.github.max_schall.appiary.data.repository.InspectionRepository
import io.github.max_schall.appiary.data.repository.TaskRepository
import io.github.max_schall.appiary.domain.model.BroodPattern
import io.github.max_schall.appiary.domain.model.ColonyStrength
import io.github.max_schall.appiary.domain.model.DiseaseConcern
import io.github.max_schall.appiary.domain.model.FoodStores
import io.github.max_schall.appiary.domain.model.PestObservation
import io.github.max_schall.appiary.domain.model.Temperament
import io.github.max_schall.appiary.domain.model.YesNoUnsure
import io.github.max_schall.appiary.domain.usecase.RefreshRecommendations
import io.github.max_schall.appiary.util.TimeUtil
import io.github.max_schall.appiary.util.newId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Fast-entry inspection form. Defaults to the common "all good" path so a
 *  normal visit is just open-and-save. */
data class InspectionForm(
    val performedAt: Long,
    val queenSeen: YesNoUnsure = YesNoUnsure.YES,
    val eggsSeen: YesNoUnsure = YesNoUnsure.YES,
    val brood: BroodPattern = BroodPattern.GOOD,
    val strength: ColonyStrength = ColonyStrength.STRONG,
    val temperament: Temperament = Temperament.CALM,
    val food: FoodStores = FoodStores.OKAY,
    val swarmSigns: Boolean = false,
    val queenCells: Boolean = false,
    val disease: DiseaseConcern = DiseaseConcern.NONE,
    val pests: PestObservation = PestObservation.NONE,
    val notes: String = "",
    val createFollowUp: Boolean = false,
    val followUpTitle: String = "",
)

class InspectionViewModel(
    savedStateHandle: SavedStateHandle,
    hiveRepo: HiveRepository,
    apiaryRepo: ApiaryRepository,
    private val inspectionRepo: InspectionRepository,
    private val taskRepo: TaskRepository,
    refreshRecommendations: RefreshRecommendations,
) : BaseLogViewModel(savedStateHandle, hiveRepo, apiaryRepo, refreshRecommendations) {

    private val _form = MutableStateFlow(InspectionForm(performedAt = clock()))
    val form = _form.asStateFlow()

    fun update(transform: (InspectionForm) -> InspectionForm) { _form.value = transform(_form.value) }

    fun save() = saveWith { hiveId, apiaryId ->
        val f = _form.value
        val now = clock()
        val followUpId = if (f.createFollowUp && f.followUpTitle.isNotBlank()) {
            taskRepo.create(
                title = f.followUpTitle, hiveId = hiveId, apiaryId = apiaryId,
                dueAt = now + TimeUtil.days(3),
            )
        } else null

        inspectionRepo.save(
            InspectionEntity(
                id = newId(), hiveId = hiveId, apiaryId = apiaryId, performedAt = f.performedAt,
                queenSeen = f.queenSeen, eggsSeen = f.eggsSeen, broodPattern = f.brood,
                strength = f.strength, temperament = f.temperament, foodStores = f.food,
                swarmSigns = f.swarmSigns, queenCells = f.queenCells, diseaseConcern = f.disease,
                pests = f.pests, notes = f.notes.ifBlank { null }, followUpTaskId = followUpId,
                createdAt = now, updatedAt = now,
            ),
        )
    }

    companion object {
        val BROOD = listOf(BroodPattern.GOOD, BroodPattern.UNEVEN, BroodPattern.WEAK, BroodPattern.NONE)
        val STRENGTH = listOf(ColonyStrength.WEAK, ColonyStrength.MODERATE, ColonyStrength.STRONG)
        val TEMPERAMENT = listOf(Temperament.CALM, Temperament.NORMAL, Temperament.DEFENSIVE)
        val FOOD = listOf(FoodStores.LOW, FoodStores.OKAY, FoodStores.STRONG)
        val YES_NO = listOf(YesNoUnsure.YES, YesNoUnsure.NO, YesNoUnsure.UNSURE)
        val DISEASE = listOf(DiseaseConcern.NONE, DiseaseConcern.SUSPECTED)
        val PESTS = PestObservation.entries
    }
}
