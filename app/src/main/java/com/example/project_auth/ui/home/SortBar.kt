package com.example.project_auth.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.project_auth.R
import com.example.project_auth.ui.common.Pill
import com.example.project_auth.ui.common.PillDefaults

/**
 * A sort icon followed by one pill per field. The active pill is filled and shows the
 * direction as an arrow; tapping it again flips the direction.
 */
@Composable
fun SortBar(order: SortOrder, onSelect: (SortField) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_sort),
            contentDescription = stringResource(R.string.sort_by),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        SortField.entries.forEach { field ->
            val selected = field == order.field
            Pill(
                text = stringResource(field.label),
                onClick = { onSelect(field) },
                colors = if (selected) PillDefaults.selected() else PillDefaults.outlined(),
                trailingIcon = when {
                    !selected -> null
                    order.ascending -> R.drawable.ic_arrow_upward
                    else -> R.drawable.ic_arrow_downward
                },
            )
        }
    }
}
