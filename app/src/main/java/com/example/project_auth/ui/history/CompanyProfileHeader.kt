package com.example.project_auth.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.project_auth.R
import com.example.project_auth.data.remote.CompanyProfile

@Composable
fun CompanyProfileHeader(profile: CompanyProfile, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!profile.logo.isNullOrBlank()) {
            AsyncImage(
                model = profile.logo,
                contentDescription = profile.name,
                modifier = Modifier.size(48.dp),
            )
        }
        Column {
            val details = listOfNotNull(profile.exchange, profile.industry).joinToString(" · ")
            if (details.isNotEmpty()) {
                Text(details, style = MaterialTheme.typography.bodyMedium)
            }
            profile.marketCap?.let {
                Text(
                    stringResource(R.string.profile_market_cap, formatMarketCap(it)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Finnhub reports market capitalization in millions of dollars. */
internal fun formatMarketCap(millions: Double): String = when {
    millions >= 1_000_000 -> "%.2f billones $".format(millions / 1_000_000)
    millions >= 1_000 -> "%.1f mil millones $".format(millions / 1_000)
    else -> "%.0f millones $".format(millions)
}
