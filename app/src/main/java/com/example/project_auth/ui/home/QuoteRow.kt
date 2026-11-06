package com.example.project_auth.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.project_auth.R
import com.example.project_auth.ui.common.CompanyLogo
import com.example.project_auth.ui.theme.changeColor

private val Star = Color(0xFFF9A825)

@Composable
fun QuoteRow(
    item: CompanyQuote,
    isFavourite: Boolean,
    onToggleFavourite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 4.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val description = stringResource(
            if (isFavourite) R.string.watchlist_remove else R.string.watchlist_add,
        )
        IconButton(
            onClick = onToggleFavourite,
            modifier = Modifier.semantics { contentDescription = description },
        ) {
            Text(
                text = if (isFavourite) "★" else "☆",
                style = MaterialTheme.typography.titleLarge,
                color = if (isFavourite) Star else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        CompanyLogo(item.company.symbol)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(item.company.symbol, style = MaterialTheme.typography.titleMedium)
            Text(
                item.company.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val quote = item.quote
        if (quote == null) {
            Text(
                stringResource(R.string.quote_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val percent = quote.changePercent ?: 0.0
            Column(horizontalAlignment = Alignment.End) {
                Text("$%.2f".format(quote.current), style = MaterialTheme.typography.titleMedium)
                Text(
                    "%+.2f%%".format(percent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = changeColor(percent),
                )
            }
        }
    }
}
