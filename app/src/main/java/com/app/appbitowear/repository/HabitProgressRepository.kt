package com.app.appbitowear.repository

import com.app.appbitowear.data.models.request.CreateHabitProgressRequest
import com.app.appbitowear.data.models.request.UpdateHabitProgressRequest
import com.app.appbitowear.data.models.response.HabitProgress
import com.app.appbitowear.data.network.apis.HabitProgressApi
import com.app.appbitowear.utils.checkSuccessOrThrow
import com.app.appbitowear.utils.safeApiCall

class HabitProgressRepository(private val api: HabitProgressApi) {

    suspend fun getProgressByDateRange(habitId: Int, startDate: String, endDate: String)
        : Result<List<HabitProgress>>{
        return safeApiCall {
            api.getProgressByDateRange(habitId, startDate, endDate)
        }
    }

    suspend fun createProgress(progress: CreateHabitProgressRequest): Result<HabitProgress> {
        return safeApiCall {
            api.createProgress(progress)
        }
    }

    suspend fun updateProgress(id: Int, progress: UpdateHabitProgressRequest)
        : Result<HabitProgress> {
        return safeApiCall {
            api.updateProgress(id, progress)
        }
    }

    suspend fun deleteProgress(id: Int): Result<Unit> {
        return safeApiCall {
            val response = api.deleteProgress(id)
            response.checkSuccessOrThrow()
        }
    }
}