package com.app.appbitowear.data.network.apis

import com.app.appbitowear.constants.ApiRoutes
import com.app.appbitowear.data.models.request.CreateHabitProgressRequest
import com.app.appbitowear.data.models.request.UpdateHabitProgressRequest
import com.app.appbitowear.data.models.response.HabitProgress
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HabitProgressApi {

    @GET("${ApiRoutes.HABITS_PROGRESS}${ApiRoutes.HABIT}/{habitId}${ApiRoutes.RANGE}")
    suspend fun getProgressByDateRange(
        @Path("habitId") habitId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): List<HabitProgress>

    @POST(ApiRoutes.HABITS_PROGRESS)
    suspend fun createProgress(@Body progress: CreateHabitProgressRequest): HabitProgress

    @PUT("${ApiRoutes.HABITS_PROGRESS}/{id}")
    suspend fun updateProgress(
        @Path("id") id: Int,
        @Body progress: UpdateHabitProgressRequest
    ): HabitProgress

    @DELETE("${ApiRoutes.HABITS_PROGRESS}/{id}")
    suspend fun deleteProgress(@Path("id") id: Int): Response<Unit>
}