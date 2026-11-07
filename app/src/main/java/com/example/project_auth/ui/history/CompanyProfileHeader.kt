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
import com.example.project_auth.domain.MarketCap
import com.example.project_auth.ui.common.CompanyLogo

@Composable
fun CompanyProfileHeader(
    symbol: String,
    profile: CompanyProfile,
    marketCap: MarketCap?,
    modifier: Modifier = Modifier,
) {
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
            marketCap?.let { MarketCapLines(it) }
        }
    }
}

/**
 * The amount in US dollars when it is known, otherwise in its own currency. When the company
 * trades in another currency, a second line gives the original amount and the exchange rate used.
 */
@Composable
private fun MarketCapLines(marketCap: MarketCap) {
    val usd = marketCap.usdMillions
    val main = if (usd != null) formatMarketCap(usd, "$") else formatMarketCap(marketCap.localMillions, marketCap.currency)
    Text(
        stringResource(R.string.profile_market_cap, main),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    val rate = marketCap.unitsPerUsd
    if (!marketCap.isUsd && usd != null && rate != null) {
        Text(
            stringResource(
                R.string.market_cap_conversion,
                formatMarketCap(marketCap.localMillions, marketCap.currency),
                "%.2f".format(rate),
                marketCap.currency,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** [millions] is in millions of [unit], which is a symbol such as "$" or a code such as "TWD". */
@Composable
private fun formatMarketCap(millions: Double, unit: String): String = when {
    millions >= 1_000_000 -> stringResource(R.string.market_cap_trillions, millions / 1_000_000, unit)
    millions >= 1_000 -> stringResource(R.string.market_cap_billions, millions / 1_000, unit)
    else -> stringResource(R.string.market_cap_millions, millions, unit)
}
