package io.github.max_schall.appiary.ui.screen.hives

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.max_schall.appiary.R
import io.github.max_schall.appiary.ui.AppViewModelProvider
import io.github.max_schall.appiary.ui.components.ApiaryFilterRowInline
import io.github.max_schall.appiary.ui.components.EmptyState
import io.github.max_schall.appiary.ui.components.HiveListItem
import io.github.max_schall.appiary.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HivesScreen(
    onOpenHive: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HivesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.nav_hives)) })

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::setQuery,
            placeholder = { Text(stringResource(R.string.hives_search)) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screen, vertical = Spacing.xs),
        )

        if (state.apiaries.size >= 2) {
            ApiaryFilterRowInline(
                apiaries = state.apiaries,
                selectedId = state.selectedApiaryId,
                onSelect = viewModel::setApiaryFilter,
                modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.xs),
            )
        }

        if (state.hives.isEmpty()) {
            EmptyState(
                Icons.Outlined.Hexagon,
                stringResource(R.string.hives_empty_title),
                stringResource(R.string.hives_empty_subtitle),
            )
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = 96.dp,
            ),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.sm),
        ) {
            items(state.hives, key = { it.hive.id }) { ui ->
                HiveListItem(ui, onClick = { onOpenHive(ui.hive.id) })
            }
        }
    }
}
