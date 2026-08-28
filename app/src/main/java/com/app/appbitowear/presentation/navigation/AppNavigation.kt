package com.app.appbitowear.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.app.appbitowear.constants.Constants
import com.app.appbitowear.constants.General
import com.app.appbitowear.constants.Login
import com.app.appbitowear.constants.Screens
import com.app.appbitowear.data.local.TokenManager
import com.app.appbitowear.data.models.response.HabitProgress
import com.app.appbitowear.presentation.components.FullScreenLoader
import com.app.appbitowear.presentation.components.GlobalToast
import com.app.appbitowear.presentation.screens.DailyHabitsScreen
import com.app.appbitowear.presentation.screens.EditProgressScreen
import com.app.appbitowear.presentation.screens.LoginScreen
import com.app.appbitowear.presentation.screens.ProfileScreen
import com.app.appbitowear.presentation.viewmodels.DailyHabitsViewModel
import com.app.appbitowear.presentation.viewmodels.EditProgressViewModel
import com.app.appbitowear.presentation.viewmodels.LoginViewModel
import com.app.appbitowear.presentation.viewmodels.ProfileViewModel
import com.app.appbitowear.repository.AuthRepository
import com.app.appbitowear.repository.HabitProgressRepository
import com.app.appbitowear.repository.HabitRepository
import com.app.appbitowear.repository.UserRepository
import com.app.appbitowear.utils.SessionManager
import com.app.appbitowear.utils.ToastManager
import com.app.appbitowear.utils.buildViewModelFactory
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private fun String.encodeUtf8(): String =
    URLEncoder.encode(this, StandardCharsets.UTF_8.name())
private fun String.decodeUtf8(): String =
    URLDecoder.decode(this, StandardCharsets.UTF_8.name())

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    habitRepository: HabitRepository,
    habitProgressRepository: HabitProgressRepository,
    userRepository: UserRepository,
    tokenManager: TokenManager
){
    val navController = rememberSwipeDismissableNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = tokenManager.getToken()
        startDestination = if (!token.isNullOrEmpty()) Screens.DAILY_HABITS else Screens.LOGIN

        SessionManager.sessionExpiredEvent.collect {
            ToastManager.showToast(Login.SESSION_EXPIRED)
            navController.navigate(Screens.LOGIN) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (startDestination == null) {
            FullScreenLoader()
        } else {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = startDestination!!
            ) {
                composable(Screens.LOGIN) {
                    val loginViewModel: LoginViewModel = viewModel(
                        factory = buildViewModelFactory { LoginViewModel(authRepository) }
                    )
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            navController.navigate(Screens.DAILY_HABITS) {
                                popUpTo(Screens.LOGIN) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screens.DAILY_HABITS) { backStackEntry ->
                    val dailyHabitsViewModel: DailyHabitsViewModel = viewModel(
                        factory = buildViewModelFactory {
                            DailyHabitsViewModel(habitRepository, habitProgressRepository)
                        }
                    )

                    val savedStateHandle = backStackEntry.savedStateHandle
                    val updateHabitId by savedStateHandle
                        .getStateFlow<Int?>(Constants.UPDATED_HABIT_ID, null)
                        .collectAsStateWithLifecycle()

                    LaunchedEffect(updateHabitId) {
                        updateHabitId?.let { habitId ->
                            val pId = savedStateHandle.get<Int>(Constants.PROGRESS_ID) ?: -1
                            val progress = if (pId != -1) {
                                HabitProgress(
                                    id = pId,
                                    date = savedStateHandle.get<String>(Constants.PROGRESS_DATE)
                                        ?: General.EMPTY_STRING,
                                    timesPerformed = savedStateHandle
                                        .get<Int>(Constants.PROGRESS_TIMES) ?: 0,
                                    note = savedStateHandle.get<String>(Constants.PROGRESS_NOTE),
                                    habitId = habitId
                                )
                            } else null

                            dailyHabitsViewModel.updateHabitProgressLocally(habitId, progress)

                            savedStateHandle.apply {
                                set(Constants.UPDATED_HABIT_ID, null)
                                set(Constants.PROGRESS_ID, null)
                                set(Constants.PROGRESS_DATE, null)
                                set(Constants.PROGRESS_TIMES, null)
                                set(Constants.PROGRESS_NOTE, null)
                            }
                        }
                    }

                    DailyHabitsScreen(
                        viewModel = dailyHabitsViewModel,
                        onEditProgress = { habitId, habitName ->
                            navController.navigate(
                            "${Screens.EDIT_PROGRESS}/$habitId/${habitName.encodeUtf8()}"
                            )
                        },
                        onNavigateToProfile = {
                            navController.navigate(Screens.USER_PROFILE)
                        }
                    )
                }

                composable(
                    route =
                    "${Screens.EDIT_PROGRESS}/{${Constants.HABIT_ID}}/{${Constants.HABIT_NAME}}",
                    arguments = listOf(
                        navArgument(Constants.HABIT_ID) { type = NavType.IntType },
                        navArgument(Constants.HABIT_NAME) { type = NavType.StringType}
                    )
                ) { backStackEntry ->
                    val arguments = backStackEntry.arguments
                    val habitId = arguments?.getInt(Constants.HABIT_ID) ?: 0
                    val habitName = arguments?.getString(Constants.HABIT_NAME)
                        ?.decodeUtf8() ?: General.EMPTY_STRING

                    val editProgressViewModel: EditProgressViewModel = viewModel(
                        factory = buildViewModelFactory {
                            EditProgressViewModel(habitProgressRepository)
                        }
                    )

                    LaunchedEffect(habitId) {
                        editProgressViewModel.loadProgressData(
                            habitId = habitId, habitName = habitName
                        )
                    }

                    EditProgressScreen(
                        viewModel = editProgressViewModel,
                        onSaveSuccess = { modifiedHabitId, finalProgress ->
                            navController.previousBackStackEntry?.savedStateHandle?.apply {
                                set(Constants.UPDATED_HABIT_ID, modifiedHabitId)
                                if (finalProgress != null) {
                                    set(Constants.PROGRESS_ID, finalProgress.id)
                                    set(Constants.PROGRESS_DATE, finalProgress.date)
                                    set(Constants.PROGRESS_TIMES, finalProgress.timesPerformed)
                                    set(Constants.PROGRESS_NOTE, finalProgress.note)
                                } else {
                                    set(Constants.PROGRESS_ID, -1)
                                }
                            }
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screens.USER_PROFILE) {
                    val profileViewModel: ProfileViewModel = viewModel(
                        factory = buildViewModelFactory {
                            ProfileViewModel(userRepository, authRepository)
                        }
                    )
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onLogoutSuccess = {
                            navController.navigate(Screens.LOGIN) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }

        GlobalToast(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}