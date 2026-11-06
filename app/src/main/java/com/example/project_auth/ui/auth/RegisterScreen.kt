package com.example.project_auth.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project_auth.R

@Composable
fun RegisterScreen(
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.auth_register),
            style = MaterialTheme.typography.headlineLarge,
        )
        EmailField(email, { email = it }, Modifier.fillMaxWidth())
        PasswordField(password, { password = it }, R.string.auth_password, Modifier.fillMaxWidth())
        PasswordField(
            value = confirmation,
            onValueChange = { confirmation = it },
            label = R.string.auth_confirm_password,
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Done,
            onDone = { viewModel.register(email, password, confirmation) },
        )
        ErrorText(state.error)
        Button(
            onClick = { viewModel.register(email, password, confirmation) },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.loading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.auth_register))
            }
        }
        TextButton(onClick = onGoToLogin) {
            Text(stringResource(R.string.auth_go_to_login))
        }
    }
}
