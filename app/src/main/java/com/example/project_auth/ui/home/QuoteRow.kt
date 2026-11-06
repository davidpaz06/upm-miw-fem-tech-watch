package com.example.project_auth.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.project_auth.R

private val Gain = Color(0xFF2E7D32)
private val Loss = Color(0xFFC62828)

@Composable
fun QuoteRow(item: CompanyQuote, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(item.company.name, style = MaterialTheme.typography.titleMedium)
            Text(
                item.company.symbol,
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
                    color = if (percent >= 0) Gain else Loss,
                )
            }
        }
    }
}
