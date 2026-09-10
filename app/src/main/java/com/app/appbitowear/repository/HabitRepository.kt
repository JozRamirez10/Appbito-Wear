package com.app.appbitowear.repository

import com.app.appbitowear.data.models.response.Habit
import com.app.appbitowear.data.network.apis.HabitApi
import com.app.appbitowear.utils.safeApiCall

class HabitRepository(private val api: HabitApi) {

    suspend fun getTodayHabits(): Result<List<Habit>> {
        return safeApiCall {
            api.getTodayHabits()
        }
    }

    suspend fun getStreakHabit(id: Int): Result<Int> {
        return safeApiCall {
            api.getStreakHabit(id)
        }
    }
}