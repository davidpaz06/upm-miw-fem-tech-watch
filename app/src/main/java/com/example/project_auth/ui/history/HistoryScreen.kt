package com.example.project_auth.ui.history

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.project_auth.R
import com.example.project_auth.data.QuoteSnapshot
import com.example.project_auth.domain.Companies
import com.example.project_auth.domain.PriceStats
import com.example.project_auth.ui.common.RoundIconButton
import com.example.project_auth.ui.theme.FavouriteColor
import com.example.project_auth.ui.theme.changeColor
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    symbol: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(
        key = symbol,
        factory = viewModelFactory { initializer { HistoryViewModel(symbol) } },
    ),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(symbol) { viewModel.refresh() }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(symbol, style = MaterialTheme.typography.titleLarge)
                Companies.find(symbol)?.let {
                    Text(
                        it.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = viewModel::toggleFavourite) {
                Icon(
                    painter = painterResource(if (state.isFavourite) R.drawable.ic_star_filled else R.drawable.ic_star),
                    contentDescription = stringResource(
                        if (state.isFavourite) R.string.watchlist_remove else R.string.watchlist_add,
                    ),
                    tint = if (state.isFavourite) FavouriteColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RoundIconButton(
                icon = R.drawable.ic_close,
                description = stringResource(R.string.detail_close),
                onClick = onClose,
            )
        }
        state.profile?.let { CompanyProfileHeader(symbol, it) }
        when {
            state.loading -> CenteredBox { CircularProgressIndicator() }

            state.failed -> CenteredBox {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.history_error))
                    Button(onClick = viewModel::load) { Text(stringResource(R.string.home_retry)) }
                }
            }

            state.snapshots.isEmpty() -> CenteredBox { Text(stringResource(R.string.history_empty)) }

            else -> LazyColumn(Modifier.fillMaxSize()) {
                item {
                    PriceChart(
                        prices = state.snapshots.map { it.price },
                        modifier = Modifier.padding(16.dp),
                    )
                    HorizontalDivider()
                    state.stats?.let { StatsSummary(it) }
                    HorizontalDivider()
                }
                items(state.snapshots.asReversed(), key = { it.timestamp }) { snapshot ->
                    SnapshotRow(snapshot)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun StatsSummary(stats: PriceStats) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StatLine(R.string.stats_max, "$%.2f".format(stats.max))
        StatLine(R.string.stats_min, "$%.2f".format(stats.min))
        StatLine(R.string.stats_average, "$%.2f".format(stats.average))
        StatLine(
            label = R.string.stats_total_change,
            value = stats.totalChangePercent?.let { "%+.2f%% ($%+.2f)".format(it, stats.totalChange) }
                ?: "—",
            color = stats.totalChangePercent?.let { changeColor(it) },
        )
        StatLine(R.string.stats_count, stats.count.toString())
    }
}

@Composable
private fun StatLine(@StringRes label: Int, value: String, color: Color? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            stringResource(label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = color ?: Color.Unspecified,
        )
    }
}

@Composable
private fun SnapshotRow(snapshot: QuoteSnapshot) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            DateFormat.getDateInstance(DateFormat.MEDIUM)
                .format(Date(snapshot.timestamp * 1000)),
            style = MaterialTheme.typography.bodyMedium,
        )
        Column(horizontalAlignment = Alignment.End) {
            Text("$%.2f".format(snapshot.price), style = MaterialTheme.typography.titleMedium)
            Text(
                "%+.2f%%".format(snapshot.changePercent),
                style = MaterialTheme.typography.bodyMedium,
                color = changeColor(snapshot.changePercent),
            )
        }
    }
}
