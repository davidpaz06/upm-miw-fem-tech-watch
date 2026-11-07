package com.example.project_auth.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * [items] sorted by [sort], as a scrollable list that jumps back to the top whenever the order
 * changes. Without that, the list keeps following the row that was on screen (rows are keyed
 * by symbol) and ends up wherever that row moved to.
 *
 * [onLastVisibleChange] receives the index, in the sorted order, of the last row on screen each
 * time it changes, so the quotes of the rows about to appear can be requested.
 */
@Composable
fun QuoteList(
    items: List<CompanyQuote>,
    sort: SortOrder,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    onLastVisibleChange: (Int) -> Unit = {},
) {
    val sorted = remember(items, sort) { items.sortedWith(sort) }
    val listState = rememberLazyListState()
    LaunchedEffect(sort) { listState.scrollToItem(0) }

    val currentOnLastVisibleChange by rememberUpdatedState(onLastVisibleChange)
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .distinctUntilChanged()
            .collect { currentOnLastVisibleChange(it) }
    }

    LazyColumn(modifier.fillMaxSize(), state = listState) {
        items(sorted, key = { it.company.symbol }) { item ->
            QuoteRow(item = item, onClick = { onOpenDetail(item.company.symbol) })
            HorizontalDivider()
        }
    }
}
