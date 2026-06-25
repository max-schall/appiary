package io.github.max_schall.appiary.ui.screen.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.data.entity.ManualTaskEntity
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.components.ExplanationSheet
import io.github.max_schall.appiary.ui.components.RecommendationCard
import io.github.max_schall.appiary.ui.components.SectionHeader
import io.github.max_schall.appiary.ui.components.ToggleRowSimple
import io.github.max_schall.appiary.ui.model.RecommendationItem
import io.github.max_schall.appiary.ui.theme.Spacing
import io.github.max_schall.appiary.ui.util.UiFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onOpenHive: (String) -> Unit,
    viewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var explaining by remember { mutableStateOf<RecommendationItem?>(null) }
    var addingTask by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.nav_tasks)) },
            actions = {
                IconButton(onClick = { addingTask = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_task))
                }
            },
        )

        LazyColumn(
            contentPadding = PaddingValues(
                start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (state.recommendations.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.section_recommendations)) }
                items(state.recommendations, key = { "rec-${it.rec.id}" }) { item ->
                    RecommendationCard(
                        item = item,
                        onPrimaryAction = { item.rec.hiveId?.let(onOpenHive) },
                        onExplain = { explaining = item },
                        onComplete = { viewModel.completeRec(item.rec.id) },
                        onSnooze = { viewModel.snoozeRec(item.rec.id) },
                        onDismiss = { viewModel.dismissRec(item.rec.id) },
                    )
                }
            }

            item { SectionHeader(stringResource(R.string.section_todo)) }
            if (state.openTasks.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.tasks_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(state.openTasks, key = { it.id }) { task ->
                TaskRow(task, done = false, onToggle = { viewModel.completeTask(task) })
            }

            if (state.doneTasks.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.section_done)) }
                items(state.doneTasks, key = { it.id }) { task ->
                    TaskRow(task, done = true, onToggle = { viewModel.reopenTask(task) })
                }
            }
        }
    }

    explaining?.let { item ->
        ExplanationSheet(item, onDismiss = { explaining = null }, onPrimaryAction = {
            explaining = null; item.rec.hiveId?.let(onOpenHive)
        })
    }

    if (addingTask) {
        AddTaskDialog(
            onDismiss = { addingTask = false },
            onAdd = { title, remind -> viewModel.addTask(title, remind); addingTask = false },
        )
    }
}

@Composable
private fun TaskRow(task: ManualTaskEntity, done: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggle) {
                Icon(
                    if (done) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = stringResource(R.string.action_done),
                    tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(Modifier.weight(1f).padding(start = Spacing.xs)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                )
                task.dueAt?.let {
                    Text(
                        stringResource(R.string.task_due, UiFormat.relativeDays(it)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, Boolean) -> Unit) {
    var title by remember { mutableStateOf("") }
    var remind by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_task)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.task_prompt)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                ToggleRowSimple(stringResource(R.string.remind_3_days), remind) { remind = it }
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(title, remind) }, enabled = title.isNotBlank()) { Text(stringResource(R.string.action_add)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
