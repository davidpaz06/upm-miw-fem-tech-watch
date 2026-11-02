package com.example.project_auth.ui.auth

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_auth.R
import com.example.project_auth.data.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    @StringRes val error: Int? = null,
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun signIn(email: String, password: String) {
        validate(email, password)?.let { return fail(it) }
        launchAuth { repository.signIn(email.trim(), password) }
    }

    fun register(email: String, password: String, confirmation: String) {
        validate(email, password)?.let { return fail(it) }
        if (password != confirmation) return fail(R.string.auth_error_password_mismatch)
        launchAuth { repository.register(email.trim(), password) }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    private fun validate(email: String, password: String): Int? = when {
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> R.string.auth_error_invalid_email
        password.length < MIN_PASSWORD_LENGTH -> R.string.auth_error_short_password
        else -> null
    }

    private fun fail(@StringRes error: Int) = _state.update { it.copy(error = error) }

    private fun launchAuth(call: suspend () -> Result<Unit>) {
        _state.value = AuthUiState(loading = true)
        viewModelScope.launch {
            val result = call()
            _state.value = AuthUiState(error = result.exceptionOrNull()?.let(::errorFor))
        }
    }

    private fun errorFor(throwable: Throwable): Int = when (throwable) {
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> R.string.auth_error_wrong_credentials
        is FirebaseAuthUserCollisionException -> R.string.auth_error_email_in_use
        is FirebaseNetworkException -> R.string.auth_error_network
        else -> R.string.auth_error_unknown
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
