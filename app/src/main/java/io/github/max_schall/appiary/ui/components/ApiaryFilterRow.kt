package io.github.max_schall.appiary.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.max_schall.appiary.ui.state.ApiaryOption
import io.github.max_schall.appiary.ui.theme.Spacing

/** Horizontal "All / <apiary>" filter chips shared by Today and Hives. */
@Composable
fun ApiaryFilterRow(
    apiaries: List<ApiaryOption>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (apiaries.size < 2) return // nothing to filter
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = Spacing.screen),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                label = { Text(androidx.compose.ui.res.stringResource(io.github.max_schall.appiary.R.string.filter_all_apiaries)) },
            )
        }
        items(apiaries.size) { i ->
            val a = apiaries[i]
            FilterChip(
                selected = selectedId == a.id,
                onClick = { onSelect(a.id) },
                label = { Text(a.name) },
            )
        }
    }
}

/** Inline row variant (non-lazy) for narrow content. */
@Composable
fun ApiaryFilterRowInline(
    apiaries: List<ApiaryOption>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        FilterChip(selectedId == null, { onSelect(null) }, { Text(androidx.compose.ui.res.stringResource(io.github.max_schall.appiary.R.string.filter_all)) })
        apiaries.forEach { a ->
            FilterChip(selectedId == a.id, { onSelect(a.id) }, { Text(a.name) })
        }
    }
}
