package com.app.appbitowear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ButtonDefaults.primaryButtonColors
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.app.appbitowear.constants.App
import com.app.appbitowear.constants.Colors
import com.app.appbitowear.constants.General
import com.app.appbitowear.constants.UserProfile
import com.app.appbitowear.presentation.components.ActionChip
import com.app.appbitowear.presentation.components.ScalingLazyColumnCustom
import com.app.appbitowear.presentation.components.ScreenTitle
import com.app.appbitowear.presentation.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listFocusRequester = remember { FocusRequester() }

    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(state.isLogoutSuccessful) {
        if (state.isLogoutSuccessful) {
            onLogoutSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        ScalingLazyColumnCustom(
            modifier = Modifier.fillMaxSize(),
            focusRequester = listFocusRequester,
            contentPadding = PaddingValues(
                top = 32.dp, bottom = 32.dp, start = 8.dp, end = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ScreenTitle(text = App.USER_PROFILE)
            }

            val currentUser = state.user

            if (state.isUserLoading && currentUser == null) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 16.dp),
                        indicatorColor = Colors.SUCCESS_GREEN
                    )
                }
            } else if (currentUser != null) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "${currentUser.name} ${currentUser.lastname}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = Center
                        )
                        Text(
                            text = currentUser.email,
                            fontSize = 12.sp,
                            color = Color.White,
                            textAlign = Center
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = UserProfile.ERROR_FETCHING_DATA,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                ActionChip(
                    text = UserProfile.LOGOUT,
                    backgroundColor = Colors.WARNING_RED,
                    onClick = { showLogoutDialog = true }
                )
            }
        }

        LogoutConfirmDialog(
            showDialog = showLogoutDialog,
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            }
        )
    }

}

@Composable
private fun LogoutConfirmDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        showDialog = showDialog,
        onDismissRequest = onDismiss
    ) {
        Alert(
            title = {
                Text(
                    text = UserProfile.LOGOUT_CONFIRMATION,
                    textAlign = Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            positiveButton = {
                Button(
                    onClick = onConfirm,
                    colors = primaryButtonColors(
                        backgroundColor = Colors.WARNING_RED
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = General.YES
                    )
                }
            },
            negativeButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = General.NO
                    )
                }
            }
        )
    }
}