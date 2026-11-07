package com.example.project_auth.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project_auth.R
import com.example.project_auth.domain.Sector
import com.example.project_auth.ui.common.Pill
import com.example.project_auth.ui.common.RoundIconButton
import com.example.project_auth.ui.common.StaleDataBanner

@Composable
fun HomeScreen(
    onOpenWatchlist: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val sortedItems = remember(state.items, state.sort) { state.items.sortedWith(state.sort) }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Pill(text = stringResource(R.string.watchlist_title), onClick = onOpenWatchlist)
            Pill(text = stringResource(R.string.stats_title), onClick = onOpenStats)
            Spacer(Modifier.weight(1f))
            RoundIconButton(
                icon = R.drawable.ic_settings,
                description = stringResource(R.string.settings_title),
                onClick = onOpenSettings,
            )
        }
        PrimaryScrollableTabRow(
            selectedTabIndex = Sector.entries.indexOf(state.sector),
            edgePadding = 0.dp,
        ) {
            Sector.entries.forEach { sector ->
                Tab(
                    selected = sector == state.sector,
                    onClick = { viewModel.selectSector(sector) },
                    text = { Text(stringResource(sector.label)) },
                )
            }
        }
        state.offlineSince?.let { StaleDataBanner(it) }
        if (state.items.isNotEmpty()) {
            SortBar(order = state.sort, onSelect = viewModel::selectSort)
        }
        PullToRefreshBox(
            isRefreshing = state.loading && state.items.isNotEmpty(),
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                state.loading && state.items.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                state.failed -> Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.home_error),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(onClick = viewModel::refresh) {
                        Text(stringResource(R.string.home_retry))
                    }
                }

                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(sortedItems, key = { it.company.symbol }) { item ->
                        QuoteRow(
                            item = item,
                            onClick = { onOpenDetail(item.company.symbol) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
