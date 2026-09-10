package com.app.appbitowear.data.network.apis

import com.app.appbitowear.constants.ApiRoutes
import com.app.appbitowear.data.models.response.Habit
import retrofit2.http.GET
import retrofit2.http.Path

interface HabitApi {

    @GET(ApiRoutes.HABITS_TODAY)
    suspend fun getTodayHabits(): List<Habit>

    @GET("${ApiRoutes.HABITS_PROGRESS}${ApiRoutes.HABIT}/{id}${ApiRoutes.STREAK}")
    suspend fun getStreakHabit(@Path("id") id: Int): Int
}