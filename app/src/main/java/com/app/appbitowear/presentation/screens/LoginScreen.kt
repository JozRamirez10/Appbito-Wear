package com.app.appbitowear.presentation.screens

import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import com.app.appbitowear.constants.App
import com.app.appbitowear.constants.Colors
import com.app.appbitowear.constants.General
import com.app.appbitowear.constants.Login
import com.app.appbitowear.presentation.components.ActionChip
import com.app.appbitowear.presentation.components.ScalingLazyColumnCustom
import com.app.appbitowear.presentation.components.ScreenTitle
import com.app.appbitowear.presentation.components.WearTextField
import com.app.appbitowear.presentation.components.handleFocus
import com.app.appbitowear.presentation.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.STARTED
    )
    var passwordVisible by remember { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current

    val listFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onLoginSuccess()
        }
    }

    ScalingLazyColumnCustom(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        focusRequester = listFocusRequester,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
    ) {
        item {
            ScreenTitle(App.NAME)
        }

        item {
            WearTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChanged,
                label = Login.EMAIL,
                keyboardType = KeyboardType.Email,
                imeAction = EditorInfo.IME_ACTION_DONE,
                onImeAction = { focusManager.clearFocus() }
            )
        }

        item {
            WearTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChanged,
                label = Login.PASSWORD,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                isPasswordVisible = passwordVisible,
                imeAction = EditorInfo.IME_ACTION_DONE,
                onImeAction = { focusManager.clearFocus() },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) General.HIDDEN
                                else General.SHOW,
                            tint = Color.LightGray
                        )
                    }
                }
            )
        }
        item {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                ActionChip(
                    text = Login.LOGIN,
                    backgroundColor = Colors.SUCCESS_GREEN,
                    onClick = {
                        handleFocus(
                            focusManager,
                            listFocusRequester,
                            viewModel::login
                        )
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }
    }
}