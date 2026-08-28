package com.app.appbitowear.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.appbitowear.constants.Constants
import com.app.appbitowear.constants.DailyHabits
import com.app.appbitowear.constants.General
import com.app.appbitowear.data.models.response.HabitProgress
import com.app.appbitowear.repository.HabitProgressRepository
import com.app.appbitowear.utils.ProgressUtils
import com.app.appbitowear.utils.ToastManager
import com.app.appbitowear.utils.handleApiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.time.LocalDate

data class EditProgressState(
    val habitId: Int = 0,
    val habitName: String = General.EMPTY_STRING,
    val timesPerformed: Int = 0,
    val note: String = General.EMPTY_STRING,
    val originalProgress: HabitProgress? = null,
    val isLoading: Boolean = true,
    val isSuccess: Boolean = false,
    val finalProgress: HabitProgress? = null
)

class EditProgressViewModel(
    private val habitProgressRepository: HabitProgressRepository
): ViewModel() {
    private val _state = MutableStateFlow(EditProgressState())
    val state: StateFlow<EditProgressState> = _state.asStateFlow()

    private val errorMessagesMap = mapOf(
        HttpURLConnection.HTTP_NOT_FOUND to DailyHabits.HABIT_NOT_FOUND,
        HttpURLConnection.HTTP_BAD_REQUEST to DailyHabits.BAD_REQUEST
    )

    fun loadProgressData(habitId: Int, habitName: String) {
        _state.update { it.copy(habitId = habitId, habitName = habitName, isLoading = true) }

        viewModelScope.launch {
            val today = LocalDate.now().toString()
            habitProgressRepository.getProgressByDateRange(
                habitId, today, today
            ).fold(
                onSuccess = { progressList ->
                    val progress = progressList.firstOrNull()
                    _state.update {
                        it.copy(
                            originalProgress = progress,
                            timesPerformed = progress?.timesPerformed ?: 0,
                            note = progress?.note ?: General.EMPTY_STRING,
                            isLoading = false
                        )
                    }
                },
                onFailure = {
                    _state.update { it.copy(isLoading = false) }
                    ToastManager.showToast(DailyHabits.PROGRESS_NOT_FOUND)
                }
            )
        }
    }

    fun incrementTimes() {
        _state.update { it.copy(timesPerformed = it.timesPerformed + 1) }
    }

    fun decrementTimes() {
        _state.update {
            if (it.timesPerformed > 0) it.copy(timesPerformed = it.timesPerformed - 1) else it
        }
    }

    fun onNoteChanged(newNote : String) {
        if (newNote.length <= Constants.LIMIT_NOTE_LENGTH) {
            _state.update { it.copy(note = newNote) }
        }
    }

    fun saveProgress() {
        val currentState = _state.value
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = ProgressUtils.saveOrUpdateProgress(
                repository = habitProgressRepository,
                habitId = currentState.habitId,
                progress = currentState.originalProgress,
                targetTimes = currentState.timesPerformed,
                note = currentState.note
            )

            handleOperationResult(result)
        }
    }

    fun deleteProgress() {
        val currentState = _state.value
        val progressId = currentState.originalProgress?.id ?: return

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = habitProgressRepository.deleteProgress(progressId).map {
                null as HabitProgress?
            }
            handleOperationResult(result)
        }
    }

    private fun handleOperationResult(result: Result<HabitProgress?>) {
        result.fold(
            onSuccess = { newProgress ->
                _state.update {
                    it.copy(isLoading = false, isSuccess = true, finalProgress = newProgress)
                }
                ToastManager.showToast(DailyHabits.EDIT_SUCCESS)
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