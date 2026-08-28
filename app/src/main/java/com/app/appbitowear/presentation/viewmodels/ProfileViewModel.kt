package com.app.appbitowear.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.appbitowear.constants.UserProfile
import com.app.appbitowear.data.models.response.User
import com.app.appbitowear.repository.AuthRepository
import com.app.appbitowear.repository.UserRepository
import com.app.appbitowear.utils.handleApiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

data class ProfileState(
    val user: User? = null,
    val isUserLoading: Boolean = true,
    val isLogoutSuccessful: Boolean = false
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
): ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val errorMessagesMap = mapOf(
        HttpURLConnection.HTTP_NOT_FOUND to UserProfile.USER_NOT_FOUND,
        HttpURLConnection.HTTP_BAD_REQUEST to UserProfile.BAD_REQUEST
    )

    fun loadUserProfile() {

        if (_state.value.user != null) return

        _state.update { it.copy(isUserLoading = true) }

        viewModelScope.launch {
            userRepository.getMe().fold(
                onSuccess = { fetchedUser ->
                    _state.update { it.copy(user = fetchedUser, isUserLoading = false) }
                },
                onFailure = { error ->
                    _state.update { it.copy(isUserLoading = false) }
                    handleApiError(
                        error = error,
                        messagesMap = errorMessagesMap
                    )
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.clearCache()
            authRepository.logout()
            _state.update { it.copy(isLogoutSuccessful = true) }
        }
    }
}