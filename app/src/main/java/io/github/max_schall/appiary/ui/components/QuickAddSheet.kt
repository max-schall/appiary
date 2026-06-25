package io.github.max_schall.appiary.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Hive
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.navigation.QuickAddAction
import io.github.max_schall.appiary.ui.theme.Spacing

/**
 * Quick-add bottom sheet — the prominent capture entry point. Lists the six
 * field actions with large touch targets for one-handed outdoor use.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onAction: (QuickAddAction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = stringResource(R.string.quick_add_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = Spacing.lg, end = Spacing.lg, bottom = Spacing.sm,
                ),
            )
            QuickAddAction.entries.forEach { action ->
                QuickAddRow(action = action, icon = action.icon(), onClick = { onAction(action) })
            }
        }
    }
}

@Composable
private fun QuickAddRow(action: QuickAddAction, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(stringResource(action.labelRes()), style = MaterialTheme.typography.bodyLarge)
        },
        leadingContent = {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(8.dp),
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

private fun QuickAddAction.icon(): ImageVector = when (this) {
    QuickAddAction.Inspection -> Icons.Outlined.Hive
    QuickAddAction.Feeding -> Icons.Filled.WaterDrop
    QuickAddAction.MiteCheck -> Icons.Outlined.BugReport
    QuickAddAction.Treatment -> Icons.Outlined.Medication
    QuickAddAction.Harvest -> Icons.Outlined.Inventory2
    QuickAddAction.Note -> Icons.Outlined.EditNote
}
