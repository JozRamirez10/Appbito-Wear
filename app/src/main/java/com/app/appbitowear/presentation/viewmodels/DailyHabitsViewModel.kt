package com.app.appbitowear.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.appbitowear.constants.DailyHabits
import com.app.appbitowear.constants.ErrorInterceptors
import com.app.appbitowear.constants.General
import com.app.appbitowear.data.models.response.Habit
import com.app.appbitowear.data.models.response.HabitProgress
import com.app.appbitowear.data.models.view.HabitView
import com.app.appbitowear.data.network.NetworkMonitor
import com.app.appbitowear.repository.HabitProgressRepository
import com.app.appbitowear.repository.HabitRepository
import com.app.appbitowear.utils.ProgressUtils
import com.app.appbitowear.utils.ToastManager
import com.app.appbitowear.utils.handleApiError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.time.LocalDate

data class DailyHabitsState(
    val habits: List<HabitView> = emptyList(),
    val isLoading: Boolean = true,
    val isInteractive: Boolean = false,
    val isNetworkAvailable: Boolean = false
)

class DailyHabitsViewModel(
    private val habitRepository: HabitRepository,
    private val habitProgressRepository: HabitProgressRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _state = MutableStateFlow(DailyHabitsState())
    val state: StateFlow<DailyHabitsState> = _state.asStateFlow()

    private var lastFetchDate: String = General.EMPTY_STRING
    private val errorMessagesMap = mapOf(
        HttpURLConnection.HTTP_NOT_FOUND to DailyHabits.HABIT_NOT_FOUND,
        HttpURLConnection.HTTP_BAD_REQUEST to DailyHabits.BAD_REQUEST
    )

    init {
        viewModelScope.launch {
            networkMonitor.isConnected.collectLatest { hastInternet ->
                _state.update { it.copy(isNetworkAvailable = hastInternet) }
                if (hastInternet && !_state.value.isInteractive
                    && _state.value.habits.isNotEmpty()) {
                    fetchHabits(LocalDate.now().toString(), isSilentRefresh = true)
                }
            }
        }
    }

    private suspend fun getStreak(habitId: Int): Int {
        return habitRepository.getStreakHabit(habitId).getOrDefault(0)
    }

    private suspend fun getTodayProgress(habitId: Int, todayStr: String) = coroutineScope {
        habitProgressRepository.getProgressByDateRange(
            habitId, todayStr, todayStr
        ).getOrNull()?.firstOrNull()
    }

    private suspend fun buildHabitViewAsync(habit: Habit, todayStr: String) = coroutineScope {
        val streakResult = async { getStreak(habit.id) }
        val progressResult = async { getTodayProgress(habit.id, todayStr) }

        val todayProgress = progressResult.await()
        val isDone = isHabitDone(todayProgress?.timesPerformed)

        HabitView(
            habit = habit,
            streak = streakResult.await(),
            isDoneToday = isDone,
            todayProgress = todayProgress
        )
    }

    fun loadTodayHabits() {
        val todayStr = LocalDate.now().toString()
        val isNewDay = lastFetchDate.isNotEmpty() && lastFetchDate != todayStr
        val hasHabits = _state.value.habits.isNotEmpty()

        _state.update { currentState ->
            currentState.copy(
                habits = if (isNewDay) emptyList() else currentState.habits,
                isLoading = isNewDay || !hasHabits,
                isInteractive = false
            )
        }

        if (isNewDay) {
            lastFetchDate = General.EMPTY_STRING
        }

        if (_state.value.isNetworkAvailable) {
            fetchHabits(todayStr, isSilentRefresh = !isNewDay && hasHabits)
        } else {
            _state.update { it.copy(isLoading = false) }
            ToastManager.showToast(ErrorInterceptors.MSG_NOT_INTERNET)
        }
    }



    fun toggleHabitDone(habitView: HabitView) {
        val habitId = habitView.habit.id

        val currentHabit = _state.value.habits.find { it.habit.id == habitId} ?: return

        viewModelScope.launch {
            updateHabitInState(habitId) { it.copy(isDoneToday = !currentHabit.isDoneToday) }

            val progress = currentHabit.todayProgress
            val targetTimes = if (currentHabit.isDoneToday) 0 else 1

            val result = ProgressUtils.saveOrUpdateProgress(
                repository = habitProgressRepository,
                habitId = habitId,
                progress = progress,
                targetTimes = targetTimes,
                note = progress?.note
            )

            result.fold(
                onSuccess = { newProgressFromBackend ->
                    val newStreak = getStreak(habitId)
                    updateHabitInState(habitId) {
                        it.copy(
                            streak = newStreak,
                            todayProgress = newProgressFromBackend
                        )
                    }
                    ToastManager.showToast(DailyHabits.EDIT_SUCCESS)
                },
                onFailure = { error ->
                    updateHabitInState(habitId) { currentHabit }
                    handleApiError(
                        error = error,
                        messagesMap = errorMessagesMap
                    )
                }
            )
        }
    }

    fun updateHabitProgressLocally(habitId: Int, progress: HabitProgress?) {
        viewModelScope.launch {
            val newStreak = getStreak(habitId)
            val isDone = isHabitDone(progress?.timesPerformed)

            updateHabitInState(habitId) {
                it.copy(
                    streak = newStreak,
                    isDoneToday = isDone,
                    todayProgress = progress
                )
            }
        }
    }

    private fun fetchHabits(todayStr: String, isSilentRefresh: Boolean) {

        if (!isSilentRefresh) _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            habitRepository.getTodayHabits().fold(
                onSuccess = { habitList ->
                    val combinedList = coroutineScope {
                        habitList.map { habit ->
                            async { buildHabitViewAsync(habit, todayStr) }
                        }.awaitAll()
                    }

                    lastFetchDate = todayStr
                    _state.update {
                        it.copy(
                            habits = combinedList,
                            isLoading = false,
                            isInteractive = true
                        )
                    }
                },
                onFailure = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isInteractive = false
                        )
                    }
                    ToastManager.showToast(ErrorInterceptors.MSG_COULD_NOT_LOAD_HABITS)
                }
            )
        }
    }

    private fun isHabitDone(timesPerformed: Int?): Boolean {
        return (timesPerformed ?: 0) >= 1
    }

    private fun updateHabitInState(habitId: Int, updateBlock: (HabitView) -> HabitView) {
        _state.update { currentState ->
            currentState.copy(
                habits = currentState.habits.map {
                    if (it.habit.id == habitId) updateBlock(it) else it
                }
            )
        }
    }
}