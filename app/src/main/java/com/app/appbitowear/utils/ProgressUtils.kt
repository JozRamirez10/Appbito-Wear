package com.app.appbitowear.utils

import com.app.appbitowear.data.models.request.CreateHabitProgressRequest
import com.app.appbitowear.data.models.request.UpdateHabitProgressRequest
import com.app.appbitowear.data.models.response.HabitProgress
import com.app.appbitowear.repository.HabitProgressRepository
import java.time.LocalDate

object ProgressUtils {

    suspend fun saveOrUpdateProgress(
        repository: HabitProgressRepository,
        habitId: Int,
        progress: HabitProgress?,
        targetTimes: Int,
        note: String?
    ): Result<HabitProgress?> {
        val hasNote = !note.isNullOrBlank()

        return when {
            progress == null ->
                repository.createProgress(
                    CreateHabitProgressRequest(
                        date = LocalDate.now().toString(),
                        timesPerformed = targetTimes,
                        note = note,
                        habitId = habitId
                    )
                ).map { it }

            targetTimes == 0 && !hasNote ->
                repository.deleteProgress(progress.id).map { null }

            else ->
                repository.updateProgress(
                    progress.id,
                    UpdateHabitProgressRequest(
                        timesPerformed = targetTimes,
                        note = note
                    )
                ).map { it }
        }
    }
}