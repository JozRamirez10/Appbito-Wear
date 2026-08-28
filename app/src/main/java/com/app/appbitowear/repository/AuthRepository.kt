package com.app.appbitowear.repository

import com.app.appbitowear.data.local.TokenManager
import com.app.appbitowear.data.models.request.LoginRequest
import com.app.appbitowear.data.network.apis.AuthApi
import com.app.appbitowear.data.network.interceptors.PersistentCookieJar
import com.app.appbitowear.utils.checkSuccessOrThrow
import com.app.appbitowear.utils.extractAndSave
import com.app.appbitowear.utils.safeApiCall
import retrofit2.Response

class AuthRepository(
    private val api: AuthApi,
    private val tokenManager: TokenManager,
    private val cookieJar: PersistentCookieJar
) {
    suspend fun login(loginRequest : LoginRequest): Result<Unit> {
        return safeApiCall {
            val response = api.login(loginRequest)
            processAuthResponse(response)
        }
    }

    suspend fun logout(): Result<Unit> {
        return safeApiCall {
            try{
                api.logout()
            } finally {
                tokenManager.clearToken()
                cookieJar.clearCookies()
            }
        }
    }

    private suspend fun processAuthResponse(response: Response<Unit>) {
        response.checkSuccessOrThrow()
        tokenManager.extractAndSave(response)
            ?: throw Exception("No se recibió el token del servidor")
    }
}
