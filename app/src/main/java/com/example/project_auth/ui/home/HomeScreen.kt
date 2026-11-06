package com.example.project_auth.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
        LazyColumn(Modifier.fillMaxSize()) {
            items(state.items, key = { it.company.symbol }) { item ->
                QuoteRow(item)
                HorizontalDivider()
            }
        }
    }
}
