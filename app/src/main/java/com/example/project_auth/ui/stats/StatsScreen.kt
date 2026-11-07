package com.example.project_auth.ui.stats

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project_auth.R
import com.example.project_auth.ui.common.BackButton
import com.example.project_auth.domain.SectorSummary
import com.example.project_auth.ui.theme.changeColor

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onClick = onBack)
            Text(stringResource(R.string.stats_title), style = MaterialTheme.typography.titleLarge)
        }
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

            else -> LazyColumn(Modifier.fillMaxSize()) {
                item {
                    Text(
                        stringResource(R.string.stats_sector_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                items(state.summaries, key = { it.sector }) { summary ->
                    SectorRow(summary)
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
private fun SectorRow(summary: SectorSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(stringResource(summary.sector.label), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(
                    R.string.stats_companies_with_data,
                    summary.companiesWithData,
                    summary.totalCompanies,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val average = summary.averageChangePercent
        if (average == null) {
            Text(
                stringResource(R.string.stats_not_enough_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                "%+.2f%%".format(average),
                style = MaterialTheme.typography.titleMedium,
                color = changeColor(average),
            )
        }
    }
}
