package com.app.appbitowear.data.network.apis

import com.app.appbitowear.constants.ApiRoutes
import com.app.appbitowear.data.models.request.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST(ApiRoutes.LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<Unit>

    @POST(ApiRoutes.AUTH_REFRESH_TOKEN)
    suspend fun refreshToken(): Response<Unit>

    @POST(ApiRoutes.AUTH_LOGOUT)
    suspend fun logout(): Response<Map<String, String>>
}