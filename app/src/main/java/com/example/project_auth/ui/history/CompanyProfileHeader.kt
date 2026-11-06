package com.example.project_auth.ui.history

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.project_auth.R
import com.example.project_auth.data.remote.CompanyProfile
import com.example.project_auth.ui.common.CompanyLogo

@Composable
fun CompanyProfileHeader(symbol: String, profile: CompanyProfile, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompanyLogo(symbol, size = 48.dp)
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
@Composable
private fun formatMarketCap(millions: Double): String = when {
    millions >= 1_000_000 -> stringResource(R.string.market_cap_trillions, millions / 1_000_000)
    millions >= 1_000 -> stringResource(R.string.market_cap_billions, millions / 1_000)
    else -> stringResource(R.string.market_cap_millions, millions)
}
