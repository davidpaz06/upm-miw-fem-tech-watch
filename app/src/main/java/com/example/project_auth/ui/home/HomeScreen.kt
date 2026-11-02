package com.example.project_auth.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.project_auth.R
import com.example.project_auth.data.AuthRepository

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    authRepository: AuthRepository = AuthRepository(),
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(authRepository.currentUser?.email.orEmpty())
        Button(onClick = authRepository::signOut) {
            Text(stringResource(R.string.auth_logout))
        }
    }
}
