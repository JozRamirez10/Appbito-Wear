package com.app.appbitowear.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.appbitowear.constants.General
import com.app.appbitowear.constants.Login
import com.app.appbitowear.data.models.request.LoginRequest
import com.app.appbitowear.repository.AuthRepository
import com.app.appbitowear.utils.ToastManager
import com.app.appbitowear.utils.handleApiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

data class LoginState(
    val email: String = General.EMPTY_STRING,
    val password: String = General.EMPTY_STRING,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val errorMessagesMap = mapOf(
        HttpURLConnection.HTTP_UNAUTHORIZED to Login.EMAIL_PASSWORD_INCORRECT,
        HttpURLConnection.HTTP_BAD_REQUEST to Login.BAD_REQUEST
    )

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun login() {
        val currentState = _state.value

        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            ToastManager.showToast(Login.EMPTY_FIELDS)
            return
        }

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val request = LoginRequest(currentState.email, currentState.password)

            authRepository.login(request).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false) }
                    handleApiError(
                        error = error,
                        messagesMap = errorMessagesMap
                    )
                }
            )
        }
    }
}
