package com.example.project_auth.ui.watchlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.example.project_auth.ui.common.StaleDataBanner
import com.example.project_auth.ui.home.QuoteRow
import com.example.project_auth.ui.home.SortBar
import com.example.project_auth.ui.home.sortedWith

@Composable
fun WatchlistScreen(
    onBack: () -> Unit,
    onOpenHistory: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchlistViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val sortedItems = remember(state.items, state.sort) { state.items.sortedWith(state.sort) }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
            Text(
                stringResource(R.string.watchlist_title),
                style = MaterialTheme.typography.titleLarge,
            )
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

                state.items.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.watchlist_empty),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(sortedItems, key = { it.company.symbol }) { item ->
                        QuoteRow(
                            item = item,
                            isFavourite = true,
                            onToggleFavourite = { viewModel.remove(item.company.symbol) },
                            onClick = { onOpenHistory(item.company.symbol) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
