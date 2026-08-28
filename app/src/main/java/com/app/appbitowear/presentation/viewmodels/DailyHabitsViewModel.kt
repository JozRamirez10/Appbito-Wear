package com.app.appbitowear.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.appbitowear.constants.DailyHabits
import com.app.appbitowear.data.models.response.Habit
import com.app.appbitowear.data.models.response.HabitProgress
import com.app.appbitowear.data.models.view.HabitView
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.time.LocalDate

data class DailyHabitsState(
    val habits: List<HabitView> = emptyList(),
    val isLoading: Boolean = true
)

class DailyHabitsViewModel(
    private val habitRepository: HabitRepository,
    private val habitProgressRepository: HabitProgressRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DailyHabitsState())
    val state: StateFlow<DailyHabitsState> = _state.asStateFlow()

    private val errorMessagesMap = mapOf(
        HttpURLConnection.HTTP_NOT_FOUND to DailyHabits.HABIT_NOT_FOUND,
        HttpURLConnection.HTTP_BAD_REQUEST to DailyHabits.BAD_REQUEST
    )

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

    fun loadTodayHabits(isSilentRefresh: Boolean = false) {
        if (!isSilentRefresh) {
            _state.update { it.copy(isLoading = true) }
        }

        viewModelScope.launch {

            val todayStr = LocalDate.now().toString()

            habitRepository.getTodayHabits().fold(
                onSuccess = { habitList ->
                    val combinedList = coroutineScope {
                        habitList.map { habit ->
                            async { buildHabitViewAsync(habit, todayStr) }
                        }.awaitAll()
                    }

                    _state.update { it.copy(habits = combinedList, isLoading = false) }
                },
                onFailure = {
                    _state.update { it.copy(isLoading = false) }
                }
            )
        }
    }

    fun toggleHabitDone(habitView: HabitView) {
        val habitId = habitView.habit.id

        viewModelScope.launch {
            updateHabitInState(habitId) { it.copy(isDoneToday = !it.isDoneToday) }

            val progress = habitView.todayProgress
            val targetTimes = if (habitView.isDoneToday) 0 else 1

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
                    updateHabitInState(habitId) { habitView }
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