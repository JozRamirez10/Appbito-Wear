package com.app.appbitowear.data.network.interceptors

import com.app.appbitowear.constants.ApiRoutes
import com.app.appbitowear.constants.Auth
import com.app.appbitowear.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection

class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val onSessionExpired: () -> Unit,
    private val onRefreshToken: suspend () -> String?
): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.url().encodedPath().contains(ApiRoutes.LOGIN)) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { tokenManager.getToken() }

        val response = chain.proceed(newRequestWithToken(originalRequest, token))

        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {

            synchronized(this) {
                return runBlocking {
                    val currentToken = tokenManager.getToken()
                    val tokenToRetry = if (currentToken != null && currentToken != token) {
                        currentToken
                    } else {
                        onRefreshToken()
                    }

                    if (!tokenToRetry.isNullOrEmpty()) {
                        response.close()
                        return@runBlocking chain.proceed(
                            newRequestWithToken(originalRequest, tokenToRetry)
                        )
                    } else {
                        tokenManager.clearToken()
                        onSessionExpired()
                        return@runBlocking response
                    }
                }
            }
        }
        return response
    }

    private fun newRequestWithToken(request: Request, token: String?): Request {
        return token?.takeIf { it.isNotEmpty() }?.let {
            request.newBuilder()
                .header(Auth.AUTHORIZATION_HEADER, "${Auth.BEARER_PREFIX}$it")
                .build()
        } ?: request
    }
}