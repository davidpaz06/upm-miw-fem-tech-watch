package com.example.project_auth.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.project_auth.R

/** One chip per field; the active one shows the direction and flips it when tapped again. */
@Composable
fun SortBar(order: SortOrder, onSelect: (SortField) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.sort_by),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SortField.entries.forEach { field ->
            val selected = field == order.field
            val arrow = if (!selected) "" else if (order.ascending) " ↑" else " ↓"
            FilterChip(
                selected = selected,
                onClick = { onSelect(field) },
                label = { Text(stringResource(field.label) + arrow) },
            )
        }
    }
}
