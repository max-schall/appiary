package io.github.max_schall.appiary.ui.screen.log

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.theme.Spacing

@Composable
fun InspectionScreen(
    onDone: () -> Unit,
    viewModel: InspectionViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val hiveOptions by viewModel.hiveOptions.collectAsStateWithLifecycle()
    val selectedHiveId by viewModel.selectedHiveId.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()

    LaunchedEffect(saved) { if (saved) onDone() }

    LogScaffold(
        title = stringResource(R.string.log_inspection),
        onClose = onDone,
        onSave = viewModel::save,
        saveEnabled = selectedHiveId != null,
    ) {
        if (selectedHiveId == null || hiveOptions.size > 1) {
            HivePickerField(hiveOptions, selectedHiveId, viewModel::selectHive)
        }

        SegmentedChoice(stringResource(R.string.field_queen_seen), InspectionViewModel.YES_NO, form.queenSeen, { stringResource(it.labelRes()) }) { v ->
            viewModel.update { it.copy(queenSeen = v) }
        }
        SegmentedChoice(stringResource(R.string.field_eggs_seen), InspectionViewModel.YES_NO, form.eggsSeen, { stringResource(it.labelRes()) }) { v ->
            viewModel.update { it.copy(eggsSeen = v) }
        }
        SegmentedChoice(stringResource(R.string.field_brood), InspectionViewModel.BROOD, form.brood, { stringResource(it.labelRes()) }) { v ->
            viewModel.update { it.copy(brood = v) }
        }
        SegmentedChoice(stringResource(R.string.field_strength), InspectionViewModel.STRENGTH, form.strength, { stringResource(it.labelRes()) }) { v ->
            viewModel.update { it.copy(strength = v) }
        }
        SegmentedChoice(stringResource(R.string.field_temperament), InspectionViewModel.TEMPERAMENT, form.temperament, { stringResource(it.labelRes()) }) { v ->
            viewModel.update { it.copy(temperament = v) }
        }
        SegmentedChoice(stringResource(R.string.field_food), InspectionViewModel.FOOD, form.food, { stringResource(it.labelRes()) }) { v ->
            viewModel.update { it.copy(food = v) }
        }

        ToggleRow(stringResource(R.string.field_swarm_signs), form.swarmSigns, { v -> viewModel.update { it.copy(swarmSigns = v) } })
        ToggleRow(stringResource(R.string.field_queen_cells), form.queenCells, { v -> viewModel.update { it.copy(queenCells = v) } })

        SegmentedChoice(stringResource(R.string.field_disease), InspectionViewModel.DISEASE, form.disease, { stringResource(it.labelRes()) }) { v ->
            viewModel.update { it.copy(disease = v) }
        }
        ChipChoice(stringResource(R.string.field_pests), InspectionViewModel.PESTS, form.pests, { stringResource(it.labelRes()) }) { v ->
            viewModel.update { it.copy(pests = v) }
        }

        NotesField(form.notes, { v -> viewModel.update { it.copy(notes = v) } })

        ToggleRow(stringResource(R.string.field_add_followup), form.createFollowUp, { v -> viewModel.update { it.copy(createFollowUp = v) } })
        if (form.createFollowUp) {
            OutlinedTextField(
                value = form.followUpTitle,
                onValueChange = { v -> viewModel.update { it.copy(followUpTitle = v) } },
                label = { Text(stringResource(R.string.field_followup_hint)) },
                singleLine = true,
                modifier = Modifier.padding(bottom = Spacing.md),
            )
        }
    }
}
