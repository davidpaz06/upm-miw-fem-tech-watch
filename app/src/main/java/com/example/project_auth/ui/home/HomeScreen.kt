package com.example.project_auth.ui.home

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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project_auth.R
import com.example.project_auth.data.AuthRepository
import com.example.project_auth.domain.Sector

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    authRepository: AuthRepository = AuthRepository(),
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                authRepository.currentUser?.email.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = authRepository::signOut) {
                Text(stringResource(R.string.auth_logout))
            }
        }
        PrimaryTabRow(selectedTabIndex = Sector.entries.indexOf(state.sector)) {
            Sector.entries.forEach { sector ->
                Tab(
                    selected = sector == state.sector,
                    onClick = { viewModel.selectSector(sector) },
                    text = { Text(stringResource(sector.label)) },
                )
            }
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
                    items(state.items, key = { it.company.symbol }) { item ->
                        QuoteRow(item)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
