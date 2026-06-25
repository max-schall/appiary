package io.github.max_schall.appiary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.max_schall.appiary.ui.i18n.labelRes
import io.github.max_schall.appiary.ui.model.RecommendationItem
import io.github.max_schall.appiary.ui.theme.Spacing
import io.github.max_schall.appiary.ui.util.UiFormat

/**
 * The "why" sheet. Surfaces the rule's plain-language reasoning plus the rule
 * key and due date, so every recommendation is explainable and auditable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplanationSheet(
    item: RecommendationItem,
    onDismiss: () -> Unit,
    onPrimaryAction: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rec = item.rec
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    rec.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                UrgencyBadge(rec.urgencyBucket)
            }

            val subtitle = listOfNotNull(item.hiveName, item.apiaryName).joinToString(" · ")
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                rec.longExplanation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.xs),
            )

            rec.dueAt?.let {
                Text(
                    "Target: ${UiFormat.relativeDays(it)} (${UiFormat.shortDate(it)})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                "Rule: ${rec.generatedFromRuleKey}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
            ) {
                Text(stringResource(rec.recommendedActionType.labelRes()))
            }
        }
    }
}
